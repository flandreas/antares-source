package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.edit.properties.ComponentPropertyPanel
import io.antarescircuit.jabbah.edit.properties.ComponentPropertyPanelController
import dev.mokkery.MockMode
import dev.mokkery.mock

class ComponentPropertyPanelMockBuilder(controller: ComponentPropertyPanelController) {

	private val view = mock<ComponentPropertyPanel>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): ComponentPropertyPanel = view
}