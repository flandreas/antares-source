package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelViewMockBuilder
import io.mockk.mockk

class GraphFrameMockBuilder(private val controller: GraphFrameController<GraphFrame>) {

	private val view = mockk<GraphFrame>(relaxed = true)

	init {
		controller.view = view
		withGraphPanelView(GraphPanelViewMockBuilder(controller.graphPanelViewController).build())
	}

	fun withGraphPanelView(view: GraphPanelView): GraphFrameMockBuilder {
		controller.graphPanelViewController.view = view
		return this
	}

	fun build(): GraphFrame = view
}