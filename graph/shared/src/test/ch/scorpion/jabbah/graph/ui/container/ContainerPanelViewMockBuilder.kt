package ch.scorpion.jabbah.graph.ui.container

import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanel
import ch.scorpion.jabbah.graph.ui.ComponentPropertyPanelMockBuilder
import io.mockk.mockk

class ContainerPanelViewMockBuilder(private val controller: ContainerPanelController) {

	private val containerPanelView = mockk<ContainerPanelView>(relaxed = true)

	init {
		controller.view = containerPanelView
		controller.drawingView.canvas = mockk(relaxed = true)
		withPropertyPanel(ComponentPropertyPanelMockBuilder(controller.propertyPanelController).build())
	}

	fun withPropertyPanel(view: ComponentPropertyPanel): ContainerPanelViewMockBuilder {
		controller.propertyPanelController.view = view
		return this
	}

	fun build(): ContainerPanelView = containerPanelView
}