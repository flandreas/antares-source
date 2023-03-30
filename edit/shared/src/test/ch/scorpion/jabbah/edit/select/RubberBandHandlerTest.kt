package ch.scorpion.jabbah.edit.select

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
}