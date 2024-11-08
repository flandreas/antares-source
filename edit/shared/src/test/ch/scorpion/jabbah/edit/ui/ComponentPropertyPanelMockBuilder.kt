package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanel
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanelController
import dev.mokkery.MockMode
import dev.mokkery.mock

class ComponentPropertyPanelMockBuilder(controller: ComponentPropertyPanelController) {

	private val view = mock<ComponentPropertyPanel>(MockMode.autofill)

	init {
		controller.view = view
	}
}