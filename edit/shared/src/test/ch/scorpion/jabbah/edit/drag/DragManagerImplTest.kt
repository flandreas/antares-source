package ch.scorpion.jabbah.edit.drag

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.Modifier
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.edit.AbstractEditIntegrationTest
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.module.EditModule
import kotlin.test.Test
import kotlin.test.assertEquals

class DragManagerImplTest : AbstractEditIntegrationTest() {

	@Test
	fun shouldDrag() {
		val rect = RectangleComponent(shape = Rectangle2D(10, 10, 20, 20))
		EditModule.drawingAppService.add(rect, editor.view)

		// Drag by 80/80. Grid snap distance is 10 by default.
		driver.mouseMoveTo(20, 20)
		driver.pressMouseAt(20, 20)
		driver.dragMouseTo(100, 100)
		driver.releaseMouseAt(100, 100)

		val newRect = editor.drawing.drawables.first() as RectangleComponent

		assertEquals(Point2D(90, 90), newRect.location)
	}

	@Test
	fun shouldDragHorizontally() {
		val rect = RectangleComponent(shape = Rectangle2D(10, 10, 20, 20))
		EditModule.drawingAppService.add(rect, editor.view)

		// Drag by 30/10, which must result in pure horizontal drag
		driver.mouseMoveTo(20, 20)
		driver.pressMouseAt(20, 20)
		driver.dragMouseTo(50, 30, Modifier.Shift.mask)
		driver.releaseMouseAt(50, 30)

		val newRect = editor.drawing.drawables.first() as RectangleComponent

		assertEquals(Point2D(40, 10), newRect.location)
	}

	@Test
	fun shouldDragVertically() {
		val rect = RectangleComponent(shape = Rectangle2D(10, 10, 20, 20))
		EditModule.drawingAppService.add(rect, editor.view)

		// Drag by 10/30, which must result in pure vertical drag
		driver.mouseMoveTo(20, 20)
		driver.pressMouseAt(20, 20)
		driver.dragMouseTo(30, 50, Modifier.Shift.mask)
		driver.releaseMouseAt(30, 50)

		val newRect = editor.drawing.drawables.first() as RectangleComponent

		assertEquals(Point2D(10, 40), newRect.location)
	}

	@Test
	fun shouldChangeOrthogonalityUponKeyPress() {
		val rect = RectangleComponent(shape = Rectangle2D(10, 10, 20, 20))
		EditModule.drawingAppService.add(rect, editor.view)
		val newRect = editor.drawing.drawables.first() as RectangleComponent

		// Drag by 80/40. Grid snap distance is 10 by default.
		driver.mouseMoveTo(20, 20)
		driver.pressMouseAt(20, 20)
		driver.dragMouseTo(100, 60)
		assertEquals(Point2D(90, 50), newRect.location)

		driver.pressKey(KeyEvent.VK_SHIFT)
		assertEquals(Point2D(90, 10), newRect.location)

		driver.releaseKey(KeyEvent.VK_SHIFT)
		assertEquals(Point2D(90, 50), newRect.location)
	}
}