package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionModel
import io.mockk.every
import io.mockk.mockk

class SelectionModelFactoryMockBuilder {

	private val factory = mockk<SelectionModelFactory>()

	init {
		withSelectionModel(mockk())
	}

	fun withSelectionModel(selectionModel: SelectionModel<Component>): SelectionModelFactoryMockBuilder {
		every { factory.create(any(), any()) } returns mockk(relaxed = true)
		return this
	}

	fun build(): SelectionModelFactory = factory
}