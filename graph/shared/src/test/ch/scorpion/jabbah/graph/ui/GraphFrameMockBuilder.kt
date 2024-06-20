package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.ui.container.ContainerPanelView
import ch.scorpion.jabbah.graph.ui.container.ContainerPanelViewMockBuilder
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelViewMockBuilder
import dev.mokkery.MockMode.autofill
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

class GraphFrameMockBuilder(private val controller: GraphFrameController<GraphFrame>) {

	private val view = mock<GraphFrame>(autofill)

	init {
		every { view.desktopView } returns mock<View<*>>(autofill)
		every { view.containerView } returns mock<View<*>>(autofill)
		every { view.applicationMode } returns ApplicationMode.EDIT

		withGraphPanelView(GraphPanelViewMockBuilder(controller.graphPanelViewController).build())
		withContainerPanelView(ContainerPanelViewMockBuilder(controller.containerPanelController).build())
		controller.view = view
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