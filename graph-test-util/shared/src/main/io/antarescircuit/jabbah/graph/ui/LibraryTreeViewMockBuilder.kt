package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeView
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import dev.mokkery.MockMode
import dev.mokkery.mock

class LibraryTreeViewMockBuilder(private val controller: LibraryTreeViewController) {

	private val view = mock<LibraryTreeView>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): LibraryTreeView = view
}