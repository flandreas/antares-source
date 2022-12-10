package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.container.ContainerPanelView
import ch.scorpion.jabbah.graph.ui.container.ContainerPanelViewMockBuilder
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelViewMockBuilder
import io.mockk.mockk

class GraphFrameMockBuilder(private val controller: GraphFrameController<GraphFrame>) {

	private val view = mockk<GraphFrame>(relaxed = true)

	init {
		controller.view = view
		withGraphPanelView(GraphPanelViewMockBuilder(controller.graphPanelViewController).build())
		withContainerPanelView(ContainerPanelViewMockBuilder(controller.containerPanelController).build())
	}

	fun withGraphPanelView(view: GraphPanelView): GraphFrameMockBuilder {
		controller.graphPanelViewController.view = view
		return this
	}

	fun withContainerPanelView(view: ContainerPanelView): GraphFrameMockBuilder {
		controller.containerPanelController.view = view
		return this
	}

	fun build(): GraphFrame = view
}