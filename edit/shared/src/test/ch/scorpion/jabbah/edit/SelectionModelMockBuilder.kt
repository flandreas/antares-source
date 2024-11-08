package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.geom.Rectangle2D
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

class SelectionModelMockBuilder {

	private val selectionModel = mock<SelectionModel<Component>>(MockMode.autofill)

	fun withComponent(component: Component): SelectionModelMockBuilder {
		every { selectionModel.component } returns component
		every { selectionModel.boundingBox } returns Rectangle2D.ZERO
		return this
	}

	fun build() = selectionModel
}