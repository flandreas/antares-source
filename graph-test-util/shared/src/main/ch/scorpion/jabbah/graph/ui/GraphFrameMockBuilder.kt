package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.ui.container.ContainerPanelView
import ch.scorpion.jabbah.graph.ui.documentation.DocumentationPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelView
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