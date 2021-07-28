package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [FlexibleTextViewTest]. */
class FlexibleTextViewTest {

	@BeforeTest
	fun beforeTest() {
		DrawTestRule.configure()
	}

	@Test
	fun test() {
		val textView = FlexibleTextView(" ", Point2D(0, 0), Direction.SOUTH, width = 100)
		assertEquals(-100.0/2 - 10 - 1, textView.boundingBox.topLeft.x)
		assertEquals(100.0/2 + 10 + 1, textView.boundingBox.bottomRight.x)
		assertEquals(0.0 - 1, textView.boundingBox.topLeft.y)
		assertEquals(textView.font.size + 10.0 + 10.0 + 1 + 1 + 4, textView.boundingBox.bottomRight.y)
	}
}