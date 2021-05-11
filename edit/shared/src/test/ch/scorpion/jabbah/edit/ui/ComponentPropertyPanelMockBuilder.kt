package ch.scorpion.jabbah.edit.ui

import io.mockk.mockk

class ComponentPropertyPanelMockBuilder(controller: ComponentPropertyPanelController) {

	private val view = mockk<ComponentPropertyPanel>(relaxed = true)

	init {
		controller.view = view
	}
}