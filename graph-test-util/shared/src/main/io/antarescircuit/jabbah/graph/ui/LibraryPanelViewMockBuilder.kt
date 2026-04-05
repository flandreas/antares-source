package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.graph.ui.library.LibraryPanelController
import io.antarescircuit.jabbah.graph.ui.library.LibraryPanelView
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeView
import dev.mokkery.MockMode
import dev.mokkery.mock

class LibraryPanelViewMockBuilder(private val controller: LibraryPanelController) {

	private val view = mock<LibraryPanelView>(MockMode.autofill)

	init {
		withLibraryTreeView(LibraryTreeViewMockBuilder(controller.libraryTreeViewController).build())
	}

	fun withLibraryTreeView(view: LibraryTreeView): LibraryPanelViewMockBuilder {
		controller.libraryTreeViewController.view = view
		return this
	}

	fun build(): LibraryPanelView = view
}