package ch.scorpion.jabbah.graph.ui.container

import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanel
import ch.scorpion.jabbah.graph.CanvasMockBuilder
import ch.scorpion.jabbah.graph.ui.ComponentPropertyPanelMockBuilder
import dev.mokkery.MockMode
import dev.mokkery.mock

class ContainerPanelViewMockBuilder(private val controller: ContainerPanelController) {

	private val containerPanelView = mock<ContainerPanelView>(MockMode.autofill)

	init {
		controller.view = containerPanelView
		controller.drawingView.canvas = CanvasMockBuilder().build()
		withPropertyPanel(ComponentPropertyPanelMockBuilder(controller.propertyPanelController).build())
	}

	fun withPropertyPanel(view: ComponentPropertyPanel): ContainerPanelViewMockBuilder {
		controller.propertyPanelController.view = view
		return this
	}

	fun build(): ContainerPanelView = containerPanelView
}