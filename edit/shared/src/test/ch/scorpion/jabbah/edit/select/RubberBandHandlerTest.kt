package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.event.Modifier
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RubberBandHandlerTest : AbstractRubberBandHandlerTest() {

	init {
		setTimer(null)
	}

	@Test
	fun shouldSelectIfFullyContained() {
		fullyEncloseRectangle()
		assertTrue(editor.view.selectionManager.isSelected(rectangle))
	}

	@Test
	fun shouldNotSelectIfNotContained() {
		notEncloseRectangle()
		assertFalse(editor.view.selectionManager.isSelected(rectangle))
	}

	@Test
	fun shouldChangeTargetStrategyWithAltKey() {
		partiallyEncloseRectangle()
		assertFalse(editor.view.selectionManager.isSelected(rectangle))

		driver.dragMouseTo(16, 16, Modifier.Alt.mask)
		assertTrue(editor.view.selectionManager.isSelected(rectangle))
	}
}