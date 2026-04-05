package io.antarescircuit.jabbah.edit.model.polyline

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.EditTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [PolylineComponent]. */
class PolylineComponentTest {

	@BeforeTest
	fun setup() {
		EditTestRule.configure()
	}

	@Test
	fun shouldMirrorHorizontally() {
		val component = PolylineComponent()
		component.polyline.addPoint(0, 0)
		component.polyline.addPoint(100, 200)

		component.mirrorHorizontally(50.0)

		assertEquals(Point2D(100.0, 0.0), component.polyline.getFirstPoint())
		assertEquals(Point2D(0.0, 200.0), component.polyline.getLastPoint())
	}

	@Test
	fun shouldMirrorVertically() {
		val component = PolylineComponent()
		component.polyline.addPoint(0, 0)
		component.polyline.addPoint(100, 200)

		component.mirrorVertically(50.0)

		assertEquals(Point2D(0.0, 100.0), component.polyline.getFirstPoint())
		assertEquals(Point2D(100.0, -100.0), component.polyline.getLastPoint())
	}
}