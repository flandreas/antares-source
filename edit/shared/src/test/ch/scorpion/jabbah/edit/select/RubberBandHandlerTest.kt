package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.edit.AbstractEditIntegrationTest
import ch.scorpion.jabbah.edit.EditorToolDriver
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.module.EditModule
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RubberBandHandlerTest : AbstractEditIntegrationTest() {

	private val driver = EditorToolDriver(editor)

	init {
		editor.selectionTool.rubberBandHandler.selectionStrategy.delaySelectTimer = null
	}

	@Test
	fun shouldSelectIfFullyContained() {
		val rect = EditModule.drawingAppService.add(
			RectangleComponent(shape = Rectangle2D(10, 10, 20, 20)),
			editor.view)

		driver.mouseMoveTo(0, 0)
		driver.pressMouseAt(0, 0)
		driver.dragMouseTo(100, 100)

		assertTrue(editor.view.selectionManager.isSelected(rect))
	}

	@Test
	fun shouldNotSelectIfNotContained() {
		val rect = EditModule.drawingAppService.add(
			RectangleComponent(shape = Rectangle2D(10, 10, 20, 20)),
			editor.view)

		driver.mouseMoveTo(100, 100)
		driver.pressMouseAt(100, 100)
		driver.dragMouseTo(200, 200)

		assertFalse(editor.view.selectionManager.isSelected(rect))
	}
}