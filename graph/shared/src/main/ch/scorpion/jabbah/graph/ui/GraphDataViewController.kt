package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.app.DefaultSavable
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.app.UnimplementedApplicationDataRepository
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.VetoException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.*

class GraphDataViewController(
	commandManager: CommandManager = EditModule.commandManager,
	eventBus: EventBus = BaseModule.eventBus
) : ApplicationDataViewController(
	commandManager = commandManager,
	newStorableProvider = { MetaGraph() },
	repository = UnimplementedApplicationDataRepository(),
	eventBus = eventBus
) {

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

	/** Implements [ApplicationDataViewController.open] by interpreting the [Savable]'s identification as a [Project] [UUID].*/
	override fun open(savable: Savable) {
		if (savable is DefaultSavable) {
			openProject(UUID(savable.identification!!))
		}
		super.open(savable)
	}

	fun openProject(uuid: UUID) {
		System.invokeLater { ProjectModule.projectManagementService.open(uuid) }
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
		if (event.item is ContainerLibraryElement && event.item == (data!!.savable as AbstractLibrarySavable).element) {
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