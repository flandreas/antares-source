package ch.scorpion.jabbah.edit

import io.mockk.every
import io.mockk.mockk

class SelectionModelMockBuilder {

	private val selectionModel = mockk<SelectionModel<Component>>(relaxed = true)

	fun withComponent(component: Component): SelectionModelMockBuilder {
		every { selectionModel.component } returns component
		return this
	}

	fun build() = selectionModel
}