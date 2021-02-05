package ch.scorpion.jabbah.graph.ui.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.draw.style.ThemeEvent
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.CurrentProjectEvent
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectHolder
import ch.scorpion.jabbah.graph.project.ProjectModule

interface LibraryPanelView : UIView {
	fun refresh()
}

/**
 * A combination of a [LibraryTreeView] and a LibraryPreviewPanel for the currently open
 * [Library] and [Project] (if any).
 *
 * Posts a [OpenContainerLibraryElementRequest] on [EventBus] when the user double clicks on a
 * [ContainerLibraryElement] in the [LibraryTreeView].
 */
class LibraryPanelController(
	applicationModeHolder: ApplicationModeHolder,
	libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	projectHolder: ProjectHolder = ProjectModule.projectHolder,
	private val eventBus: EventBus
) : AbstractUIController<LibraryPanelView>() {

	val libraryTreeViewController = LibraryTreeViewController(LibraryTreeViewType.Main, libraryHolder.library, projectHolder.project, applicationModeHolder, eventBus)
	private val themeHandler: EventHandler<ThemeEvent> = { view.refresh() }
	private val currentLibraryHandler: EventHandler<CurrentLibraryEvent> = { libraryTreeViewController.library = it.library }
	private val currentProjectHandler: EventHandler<CurrentProjectEvent> = { libraryTreeViewController.project = it.project }

	init {
		eventBus.register(ThemeEvent::class, themeHandler)
		eventBus.register(CurrentLibraryEvent::class, currentLibraryHandler)
		eventBus.register(CurrentProjectEvent::class, currentProjectHandler)
	}

	override fun dispose() {
		super.dispose()
		libraryTreeViewController.dispose()
		eventBus.unregister(themeHandler)
		eventBus.unregister(currentLibraryHandler)
		eventBus.unregister(currentProjectHandler)
	}

	fun requestOpenSelectedContainerLibraryElement() {
		val item = libraryTreeViewController.selectedItem
		if (item is ContainerLibraryElement) {
			eventBus.post(OpenContainerLibraryElementRequest(item))
		}
	}
}