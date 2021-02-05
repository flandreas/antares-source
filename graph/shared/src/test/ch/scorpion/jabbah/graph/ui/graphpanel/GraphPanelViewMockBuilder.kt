package ch.scorpion.jabbah.graph.ui.graphpanel

import ch.scorpion.jabbah.graph.ui.GraphDesktopView
import ch.scorpion.jabbah.graph.ui.GraphEditView
import ch.scorpion.jabbah.graph.ui.GraphEditViewMockBuilder
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewMockBuilder
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelView
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelViewMockBuilder
import io.mockk.mockk

class GraphPanelViewMockBuilder(private val controller: GraphPanelViewController) {

	private val view = mockk<GraphPanelView>()

	init {
		controller.view = view
		withLibraryPanel(LibraryPanelViewMockBuilder(controller.libraryPanelController).build())
		withGraphEditView(GraphEditViewMockBuilder(controller.editViewController).build())
		withGraphDesktopView(GraphDesktopViewMockBuilder(controller.desktopController).build())
	}

	fun withLibraryPanel(view: LibraryPanelView): GraphPanelViewMockBuilder {
		controller.libraryPanelController.view = view
		return this
	}

	fun withGraphEditView(view: GraphEditView): GraphPanelViewMockBuilder {
		controller.editViewController.view = view
		return this
	}

	fun withGraphDesktopView(view: GraphDesktopView): GraphPanelViewMockBuilder {
		controller.desktopController.view = view
		return this
	}
}