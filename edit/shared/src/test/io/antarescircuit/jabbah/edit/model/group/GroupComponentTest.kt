package io.antarescircuit.jabbah.edit.model.group

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.edit.EditTestRule
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [GroupComponent].*/
class GroupComponentTest {

	@BeforeTest
	fun setup() {
		EditTestRule.configure()
	}
	@Test
	fun shouldHaveCombinedBoundingBox() {
		val group = GroupComponent(listOf(
			RectangleComponent(0.0, 0.0, 100.0, 100.0),
			RectangleComponent(200.0, 200.0, 100.0, 100.0)
		))
		assertEquals(Rectangle2D(-1.0, -1.0, 302.0, 302.0), group.boundingBox as Rectangle2D)
	}
}