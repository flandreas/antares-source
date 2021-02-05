package ch.scorpion.jabbah.graph.ui.library

import io.mockk.mockk

class LibraryPanelViewMockBuilder(private val controller: LibraryPanelController) {

	private val view = mockk<LibraryPanelView>(relaxed = true)

	init {
		withLibraryTreeView(LibraryTreeViewMockBuilder(controller.libraryTreeViewController).build())
	}

	fun withLibraryTreeView(view: LibraryTreeView): LibraryPanelViewMockBuilder {
		controller.libraryTreeViewController.view = view
		return this
	}

	fun build(): LibraryPanelView = view
}