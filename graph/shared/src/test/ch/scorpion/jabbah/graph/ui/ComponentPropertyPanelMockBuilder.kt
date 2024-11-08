package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanel
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanelController
import dev.mokkery.MockMode
import dev.mokkery.mock

// TODO: This should be located in the edit module, but cannot yet be imported as test artefact
class ComponentPropertyPanelMockBuilder(controller: ComponentPropertyPanelController) {

	private val view = mock<ComponentPropertyPanel>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): ComponentPropertyPanel = view
}