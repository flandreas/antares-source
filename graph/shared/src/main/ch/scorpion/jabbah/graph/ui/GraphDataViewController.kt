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
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.*
import ch.scorpion.jabbah.io.Storable

/**
 * An [ApplicationDataViewController] that treats [ApplicationData.content] as [MetaGraph]
 * and [ApplicationData.savable] as [AbstractLibrarySavable].
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

	private val openProjectRequestHandler: EventHandler<OpenProjectRequest> = { handle(it) }
	private val closeProjectRequestHandler: EventHandler<CloseProjectRequest> = { handle(it) }
	private val currentProjectEventHandler: EventHandler<CurrentProjectEvent> = { closeData() }
	private val openLibraryRequestHandler: EventHandler<OpenLibraryRequest> = { handle(it) }
	private val currentLibraryEventHandler: EventHandler<CurrentLibraryEvent> = { closeData() }
	private val libraryItemRemovedHandler: EventHandler<LibraryItemRemovedEvent> = { handle(it) }
	private val closeQuestionHandler: EventHandler<GraphDesktopViewItemCloseQuestion> = { handle(it) }
	private val closeRequestHandler: EventHandler<GraphDesktopViewItemCloseRequest> = { handle(it) }

	init {
		eventBus.register(OpenProjectRequest::class, openProjectRequestHandler)
		eventBus.register(CloseProjectRequest::class, closeProjectRequestHandler)
		eventBus.register(CurrentProjectEvent::class, currentProjectEventHandler)
		eventBus.register(OpenLibraryRequest::class, openLibraryRequestHandler)
		eventBus.register(CurrentLibraryEvent::class, currentLibraryEventHandler)
		eventBus.register(LibraryItemRemovedEvent::class, libraryItemRemovedHandler)
		eventBus.register(GraphDesktopViewItemCloseQuestion::class, closeQuestionHandler)
		eventBus.register(GraphDesktopViewItemCloseRequest::class, closeRequestHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(openProjectRequestHandler)
		eventBus.unregister(closeProjectRequestHandler)
		eventBus.unregister(currentProjectEventHandler)
		eventBus.unregister(openLibraryRequestHandler)
		eventBus.unregister(currentLibraryEventHandler)
		eventBus.unregister(libraryItemRemovedHandler)
		eventBus.unregister(closeQuestionHandler)
		eventBus.unregister(closeRequestHandler)
	}

	override fun setUndoableState(state: Storable) {
		if (data?.savable is AbstractLibrarySavable && state is MetaGraph) {
			(data?.savable as AbstractLibrarySavable).element.updateMetaGraph(state)
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
		System.invokeLater { ProjectModule.projectManagementService.invoke().open(identification) }
	}

	fun openAsSavable(element: ContainerLibraryElement, actionName: String) {
		try {
			LOG.info("Open '${element.name.value}'")
			view.registerKeepAliveUsage()

			open {
				val library = element.library!!
				library.libraryService.loadMetaGraph(library, element)
				ApplicationData(element.metaGraph!!, library.createSavable(element), eventBus)
			}
		} catch (e: Throwable) {
			LOG.error("Error while loading ${element.uuid}: ${e.message}")
			view.showModalMessage(
				ModalMessageType.Error,
				actionName,
				Translations.getString("graph.action.load.error.general.desc"))
		}
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: OpenProjectRequest) {
		if (!canReplaceSavable("project.action.open.name")) {
			throw VetoException(Translations.getString("application.replaceSavableVeto.msg"))
		}
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: OpenLibraryRequest) {
		if (!canReplaceSavable("library.action.open.name")) {
			throw VetoException(Translations.getString("application.replaceSavableVeto.msg"))
		}
	}

	private fun handle(event: CloseProjectRequest) {
		if (data?.savable is ProjectSavable && (data!!.savable as ProjectSavable).project == event.project && !canReplaceSavable("project.action.close.name")) {
			throw VetoException(Translations.getString("application.replaceSavableVeto.msg"))
		}
	}

	private fun handle(event: LibraryItemRemovedEvent) {
		if (event.item is ContainerLibraryElement && event.item == (data?.savable as AbstractLibrarySavable?)?.element) {
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
}