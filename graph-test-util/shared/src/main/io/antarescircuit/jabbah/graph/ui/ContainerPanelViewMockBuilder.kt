package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.draw.CanvasMockBuilder
import io.antarescircuit.jabbah.edit.ComponentPropertyPanelMockBuilder
import io.antarescircuit.jabbah.edit.properties.ComponentPropertyPanel
import io.antarescircuit.jabbah.graph.ui.container.ContainerPanelController
import io.antarescircuit.jabbah.graph.ui.container.ContainerPanelView
import io.antarescircuit.jabbah.graph.ui.container.SymbolComparatorView
import dev.mokkery.MockMode
import dev.mokkery.mock

class ContainerPanelViewMockBuilder(private val controller: ContainerPanelController) {

	private val containerPanelView = mock<ContainerPanelView>(MockMode.autofill)

	init {
		controller.view = containerPanelView
		controller.drawingView.canvas = CanvasMockBuilder().build()
		withPropertyPanel(ComponentPropertyPanelMockBuilder(controller.propertyPanelController).build())
		withSymbolComparatorView(SymbolComparatorViewMockBuilder(controller.symbolComparatorController).build())
	}

	fun withPropertyPanel(view: ComponentPropertyPanel): ContainerPanelViewMockBuilder {
		controller.propertyPanelController.view = view
		return this
	}

	fun withSymbolComparatorView(view: SymbolComparatorView): ContainerPanelViewMockBuilder {
		controller.symbolComparatorController.view = view
		return this
	}

	fun build(): ContainerPanelView = containerPanelView
}