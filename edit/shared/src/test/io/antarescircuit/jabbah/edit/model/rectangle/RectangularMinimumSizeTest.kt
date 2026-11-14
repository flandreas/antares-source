package io.antarescircuit.jabbah.edit.model.rectangle

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractHandleSelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractSelectedColorWrappingSelectionModel
import kotlin.test.Test
import kotlin.test.assertEquals

class RectangularMinimumSizeTest : AbstractRectangularHandleSelectionModelTest() {

	override fun createSelectionModel(component: AbstractRectangularComponent): SelectionModel<Component> {
		val geometry = object : ResizableRectangularComponent, Component by component {
			override val shape get() = component.shape
			override val minimumWidth = 80.0
			override val minimumHeight = 40.0
			override fun setFrame(x: Double, y: Double, width: Double, height: Double) = component.setFrame(x, y, width, height)
		}
		return object : AbstractSelectedColorWrappingSelectionModel<ResizableRectangularComponent>(geometry) {
			override fun createInnerSelectionModel(component: ResizableRectangularComponent): AbstractHandleSelectionModel<ResizableRectangularComponent> = RectangularHandleSelectionModel(component)
		}
	}

	@Test
	fun shouldClampNorthWestAndKeepOppositeCorner() {
		val rect = addSelectedRect(0, 0, 200, 100)
		driver.pressAndDragTo(0, 0, 190, 90)
		assertEquals(Rectangle2D(120, 60, 80, 40), rect.shape)
	}

	@Test
	fun shouldClampSouthEastAndKeepOppositeCorner() {
		val rect = addSelectedRect(0, 0, 200, 100)
		driver.pressAndDragTo(200, 100, 10, 10)
		assertEquals(Rectangle2D(0, 0, 80, 40), rect.shape)
	}
}
