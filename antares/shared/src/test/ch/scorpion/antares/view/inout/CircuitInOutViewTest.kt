package ch.scorpion.antares.view.inout

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.geom.Point2D
import kotlin.test.Test
import kotlin.test.assertEquals

class CircuitInOutViewTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldNotChangeLocationWhenChangingBitWidthWhileConnected() {
		val builder = TestCircuitBuilder("test")
		val input1 = builder.addInput()
		val input2 = builder.addInput()
		input1.location = Point2D.ZERO
		input2.location = Point2D(0, 100)
		builder.connectOutputOpen(input1, Point2D(100, 0))

		input1.bitWidth = BitWidth.BW_2
		input2.bitWidth = BitWidth.BW_2

		assertEquals(Point2D.ZERO, input1.location)
		assertEquals(input2.boundingBox.minX, input1.boundingBox.minX)
	}
}