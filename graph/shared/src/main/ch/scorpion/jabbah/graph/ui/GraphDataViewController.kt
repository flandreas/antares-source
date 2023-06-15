package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.VetoException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.ui.Toast
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemCloseQuestion
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemCloseRequest
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCloner

/**
 * An [ApplicationDataViewController] that treats [ApplicationData.content] as [MetaGraph]
 * and [ApplicationData.savable] as [AbstractContainerLibraryElementSavable].
 */
class GraphDataViewController(
	commandManager: CommandManager = EditModule.commandManager,
	eventBus: EventBus = BaseModule.eventBus
) : ApplicationDataViewController(
	commandManager = commandManager,
	newStorableProvider = { MetaGraph() },
	repository = UnimplementedApplicationDataRepository(),
	eventBus = eventBus
) {
	companion object {
		private val LOG by logger(GraphDataViewController::class)
	}

	private val openLibraryRequestHandler: EventHandler<OpenLibraryRequest> = { handle(it) }
	private val closeLibraryRequestHandler: EventHandler<CloseLibraryRequest> = { handle(it) }
	private val currentLibraryEventHandler: EventHandler<CurrentLibraryEvent> = {
		if (it.library == null) {
			closeData()
		}
	}
	private val libraryItemRemovedHandler: EventHandler<LibraryItemRemovedEvent> = { handle(it) }
	private val closeQuestionHandler: EventHandler<GraphDesktopViewItemCloseQuestion> = { handle(it) }
	private val closeRequestHandler: EventHandler<GraphDesktopViewItemCloseRequest> = { handle(it) }
	private val libraryImportRemoveQuestionHandler: EventHandler<LibraryImportRemoveQuestion> = { handle(it) }
	private val libraryImportRemovedHandler: EventHandler<LibraryImportRemovedEvent> = { handle(it) }
	private val currentWorkspaceHandler: EventHandler<CurrentWorkspaceEvent> = { handle(it) }

	init {
		eventBus.register(OpenLibraryRequest::class, openLibraryRequestHandler)
		eventBus.register(CloseLibraryRequest::class, closeLibraryRequestHandler)
		eventBus.register(CurrentLibraryEvent::class, currentLibraryEventHandler)
		eventBus.register(LibraryItemRemovedEvent::class, libraryItemRemovedHandler)
		eventBus.register(GraphDesktopViewItemCloseQuestion::class, closeQuestionHandler)
		eventBus.register(GraphDesktopViewItemCloseRequest::class, closeRequestHandler)
		eventBus.register(LibraryImportRemoveQuestion::class, libraryImportRemoveQuestionHandler)
		eventBus.register(LibraryImportRemovedEvent::class, libraryImportRemovedHandler)
		eventBus.register(CurrentWorkspaceEvent::class, currentWorkspaceHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(openLibraryRequestHandler)
		eventBus.unregister(closeLibraryRequestHandler)
		eventBus.unregister(currentLibraryEventHandler)
		eventBus.unregister(libraryItemRemovedHandler)
		eventBus.unregister(closeQuestionHandler)
		eventBus.unregister(closeRequestHandler)
		eventBus.unregister(libraryImportRemoveQuestionHandler)
		eventBus.unregister(libraryImportRemovedHandler)
		eventBus.unregister(currentWorkspaceHandler)
	}

	override fun setUndoableState(state: Storable) {
		if (data?.savable is AbstractLibraryItemSavable) {
			val savable = data!!.savable as AbstractLibraryItemSavable
			if (savable.item is UndoableStateLibraryItem<*>) {
				(savable.item as UndoableStateLibraryItem<Storable>).updateStorable(state)
			}
		}
		super.setUndoableState(state)
	}

	/**
	 * Implements [ApplicationDataViewController.open] by interpreting the [Savable]'s identification as a [Project] [UUID],
	 * whose opening results in opening the default [LibraryElement] of the [Project].
	 * */
	override fun open(savable: Savable) {
		if (savable is DefaultSavable) {
			openProject(LibraryIdentification(UUID(savable.identification!!), EditAuthModule.userHolder.user.identity))
		}
		super.open(savable)
	}

	fun openProject(identification: LibraryIdentification) {
		System.invokeLater { ProjectModule.projectManagementService.open(identification) }
	}

	fun openAsSavable(element: ContainerLibraryElement, actionName: String) {
		try {
			LOG.info("Open '${element.name.value}'")
			view.registerKeepAliveUsage()

			open {
				val library = element.library!!
				library.libraryService.loadMetaGraph(library, element)
				ApplicationData(StorableCloner.clone(element.metaGraph!!), library.createSavable(element), eventBus)
			}
		} catch (e: Throwable) {
			LOG.error("Error while loading ${element.uuid}: ${e.message}")
			view.showModalMessage(
				ModalMessageType.Error,
				actionName,
				Translations.getString("graph.action.load.error.general.desc"))
		}
	}

	fun openAsStorable(content: Storable, savable: Savable) {
		open {
			ApplicationData(content, savable)
		}
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: OpenLibraryRequest) {
		if (!canReplaceSavable("library.action.open.name")) {
			throw VetoException(Translations.getString("application.replaceSavableVeto.msg"))
		}
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: CloseLibraryRequest) {
		if (!canReplaceSavable("project.action.close.name")) {
			throw VetoException(Translations.getString("application.replaceSavableVeto.msg"))
		}
	}

	private fun handle(event: LibraryItemRemovedEvent) {
		if (event.item is UndoableStateLibraryItem<*> && event.item == (data?.savable as? AbstractLibraryItemSavable?)?.item) {
			System.invokeLater { closeData() }
		}
	}

	private fun handle(event: GraphDesktopViewItemCloseQuestion) {
		if (event.isRoot && !canReplaceSavable("base.action.close.name")) {
			throw VetoException(Translations.getString("application.replaceSavableVeto.msg"))
		}
	}

	private fun handle(event: GraphDesktopViewItemCloseRequest) {
		if (event.isRoot) {
			closeDataAfterConfirmation()
		}
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: LibraryImportRemoveQuestion) {
		if (!canReplaceSavable("project.action.close.name")) {
			throw VetoException(Translations.getString("application.replaceSavableVeto.msg"))
		}
	}

	private fun handle(event: LibraryImportRemovedEvent) {
		if (event.libraryId == (data?.savable as? AbstractLibraryItemSavable)?.item?.library?.uuid) {
			closeDataAfterConfirmation()
		}
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: CurrentWorkspaceEvent) {
		if (event.isPrepare) {
			if (!canReplaceSavable("application.workspace.dialog.title")) {
				throw VetoException(Translations.getString("application.replaceSavableVeto.msg"))
			}
			return
		}
		if (!event.isPrepare) {
			LibraryModule.libraryManagementService.close()
			Toast.show(Translations.getString("graph.workspace.msg"))
		}
	}
}