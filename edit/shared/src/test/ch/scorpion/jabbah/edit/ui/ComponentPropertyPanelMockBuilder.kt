package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.edit.Component
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class ComponentPropertyPanelMockBuilder(controller: ComponentPropertyPanelController) {

	private val view = mockk<ComponentPropertyPanel>(relaxed = true)
	val beanSlot = slot<Any>()
	val componentSlot = slot<Component>()

	init {
		controller.view = view
		every { view.loadProperties(capture(beanSlot)) } returns Unit
		every { view.loadComponentProperties(capture(componentSlot)) } returns Unit
	}
}