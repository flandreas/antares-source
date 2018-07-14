package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawTestRule
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [FlexibleTextViewTest]. */
class FlexibleTextViewTest {

	companion object {
		@ClassRule
		@JvmField
		val drawTestRule = DrawTestRule()
	}

	@Test
	fun test() {
		val textView = FlexibleTextView("", Point2D(0, 0), Direction.SOUTH, width = 100)
		assertThat(textView.boundingBox.topLeft.x, `is`(-100.0/2 - 10 - 1))
		assertThat(textView.boundingBox.bottomRight.x, `is`(100.0/2 + 10 + 1))
		assertThat(textView.boundingBox.topLeft.y, `is`(0.0 - 1))
		assertThat(textView.boundingBox.bottomRight.y, `is`(textView.font.size + 10.0 + 10.0 + 1 + 1 + 4))
	}
}