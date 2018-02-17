package ch.scorpion.jabbah.edit.model.group

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [GroupComponent].*/
class GroupComponentTest {

	companion object {
		@ClassRule @JvmField
		val editTestRule = EditTestRule()
	}

	@Test
	fun shouldHaveCombinedBoundingBox() {
		val group = GroupComponent(listOf(
			RectangleComponent(0.0, 0.0, 100.0, 100.0),
			RectangleComponent(200.0, 200.0, 100.0, 100.0)
		))
		assertThat(group.boundingBox as Rectangle2D, `is`(Rectangle2D(-1.0, -1.0, 302.0, 302.0)))
	}
}