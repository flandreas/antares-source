package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.ui.container.ContainerPanelView
import io.antarescircuit.jabbah.graph.ui.documentation.DocumentationPanelView
import io.antarescircuit.jabbah.graph.ui.graphpanel.GraphPanelView
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

class GraphFrameMockBuilder<T: GraphFrame>(private val controller: GraphFrameController<T>) {

	private val view = mock<GraphFrame>(MockMode.autofill)

	val graphPanelViewBuilder = GraphPanelViewMockBuilder(controller.graphPanelViewController)

	init {
		every { view.desktopView } returns mock<View<*>>(MockMode.autofill)
		every { view.containerView } returns mock<View<*>>(MockMode.autofill)
		every { view.applicationMode } returns ApplicationMode.EDIT

		withGraphPanelView(graphPanelViewBuilder.build())
		withContainerPanelView(ContainerPanelViewMockBuilder(controller.containerPanelController).build())
		withDocumentationPanelView(DocumentationPanelViewMockBuilder(controller.documentationPanelController).build())
		controller.view = view as T
	}

	fun withGraphPanelView(view: GraphPanelView): GraphFrameMockBuilder<T> {
		controller.graphPanelViewController.view = view
		return this
	}

	fun withContainerPanelView(view: ContainerPanelView): GraphFrameMockBuilder<T> {
		controller.containerPanelController.view = view
		return this
	}

	fun withDocumentationPanelView(view: DocumentationPanelView): GraphFrameMockBuilder<T> {
		controller.documentationPanelController.view = view
		return this
	}

	fun build(): GraphFrame = view
}