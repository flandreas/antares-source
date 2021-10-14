package ch.scorpion.antares.view.inout

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.drawable.DrawableAttendantPositioner
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class CircuitInOutKeyboardTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldCalculateBoundingBox() {
		val circuitInOutView = circuitInOutMock(Rectangle2D(100, 100, 100, 50))
		val position = Point2D(200, 200)
		val keyboard = CircuitInOutKeyboard(
			circuitInOutView,
			view = mockk(),
			contextHolder = mockk(),
			positioner = positioner(position))

		assertEquals(Rectangle2D(position, Dimension2D(CircuitInOutKeyboard.KEYBOARD_WIDTH, CircuitInOutKeyboard.KEYBOARD_HEIGHT)), keyboard.boundingBox)
	}

	private fun circuitInOutMock(bbox: Rectangle2D): CircuitInOutView {
		val mock = mockk<CircuitInOutView>()
		every { mock.boundingBox } returns bbox
		every { mock.signalRepresentation } returns DigitalSignalRepresentation.HEXADECIMAL
		return mock
	}

	private fun positioner(position: Point2D): DrawableAttendantPositioner {
		val mock = mockk<DrawableAttendantPositioner>()
		every { mock.position(any(), any(), any(), any(), any()) } returns position
		return mock
	}
}