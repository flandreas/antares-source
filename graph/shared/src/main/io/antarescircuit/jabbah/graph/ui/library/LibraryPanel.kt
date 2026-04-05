package io.antarescircuit.jabbah.graph.ui.library

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.draw.style.ThemeEvent
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.project.Project

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
	editor: Editor? = null,
	libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<LibraryPanelView>() {

	val libraryTreeViewController = LibraryTreeViewController(LibraryTreeViewType.Main, libraryHolder.l, applicationModeHolder, editor, eventBus)
	val libraryTreePanelController = LibraryTreePanelController(libraryTreeViewController)

	private val themeHandler: EventHandler<ThemeEvent> = { view.refresh() }
	private val currentLibraryHandler: EventHandler<CurrentLibraryEvent> = {
		libraryTreeViewController.library = it.library
	}

	init {
		eventBus.register(ThemeEvent::class, themeHandler)
		eventBus.register(CurrentLibraryEvent::class, currentLibraryHandler)
	}

	override fun dispose() {
		super.dispose()
		libraryTreeViewController.dispose()
		libraryTreePanelController.dispose()
		eventBus.unregister(themeHandler)
		eventBus.unregister(currentLibraryHandler)
	}
}