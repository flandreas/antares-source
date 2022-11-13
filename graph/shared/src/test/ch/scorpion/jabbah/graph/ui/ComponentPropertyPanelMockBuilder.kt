package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanel
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanelController
import io.mockk.mockk

// TODO: This should be located in the edit module, but cannot yet be imported as test artefact
class ComponentPropertyPanelMockBuilder(controller: ComponentPropertyPanelController) {

	private val view = mockk<ComponentPropertyPanel>(relaxed = true)

	init {
		controller.view = view
	}

	fun build(): ComponentPropertyPanel = view
}