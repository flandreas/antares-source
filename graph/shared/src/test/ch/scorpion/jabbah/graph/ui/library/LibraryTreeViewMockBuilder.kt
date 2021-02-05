package ch.scorpion.jabbah.graph.ui.library

import io.mockk.mockk

class LibraryTreeViewMockBuilder(private val controller: LibraryTreeViewController) {

	private val view = mockk<LibraryTreeView>(relaxed = true)

	init {
		controller.view = view
	}

	fun build(): LibraryTreeView = view
}