package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionModel
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

class SelectionModelFactoryMockBuilder {

	private val factory = mock<SelectionModelFactory>()

	init {
		val sm = mock<SelectionModel<*>>(MockMode.autofill)
		every { sm.boundingBox } returns Rectangle2D()
		withSelectionModel(sm)
	}

	fun withSelectionModel(selectionModel: SelectionModel<Component>): SelectionModelFactoryMockBuilder {
		every { factory.create(any(), any()) } returns selectionModel
		return this
	}

	fun build(): SelectionModelFactory = factory
}