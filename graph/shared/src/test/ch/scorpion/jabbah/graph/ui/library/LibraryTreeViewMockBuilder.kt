package ch.scorpion.jabbah.graph.ui.library

import dev.mokkery.MockMode
import dev.mokkery.mock

class LibraryTreeViewMockBuilder(private val controller: LibraryTreeViewController) {

	private val view = mock<LibraryTreeView>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): LibraryTreeView = view
}