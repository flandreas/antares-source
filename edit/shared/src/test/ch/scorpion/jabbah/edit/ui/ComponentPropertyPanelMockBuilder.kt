package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanel
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanelController
import io.mockk.mockk

class ComponentPropertyPanelMockBuilder(controller: ComponentPropertyPanelController) {

	private val view = mockk<ComponentPropertyPanel>(relaxed = true)

	init {
		controller.view = view
	}
}