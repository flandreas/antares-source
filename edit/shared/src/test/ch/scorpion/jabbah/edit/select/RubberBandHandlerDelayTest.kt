package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.ControlledTimer
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Tests delaying containment checks using a timer.*/
class RubberBandHandlerDelayTest : AbstractRubberBandHandlerTest() {

	private val timeService = ControlledTimeService()
	private val timer = ControlledTimer(timeService)

	init {
		setTimer(timer)
	}

	@Test
	fun shouldDelayContainsCheck() {
		fullyEncloseRectangle()
		assertFalse(editor.view.selectionManager.isSelected(rectangle))

		timeService.setTimeMillis(timer.interval.toLong())
		assertTrue(editor.view.selectionManager.isSelected(rectangle))
	}
}