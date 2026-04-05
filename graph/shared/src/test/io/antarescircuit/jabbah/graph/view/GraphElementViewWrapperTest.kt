package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.drawable.RotationDirection
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.model.curve.QuadCurveComponent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraphElementViewWrapperTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
	}

	@Test
	fun shouldBeRotatable() {
		val curve = QuadCurveComponent(listOf(
			Point2D(0, 0),
			Point2D(100, -100),
			Point2D(200, 0)
		))
		val wrapper = GraphElementViewWrapper(curve)
		assertTrue(wrapper.isRotatableWith(listOf<Component>()))

		wrapper.rotate(RotationDirection.Clockwise)
		assertEquals(
			listOf(
				Point2D(0, 0),
				Point2D(100, 100),
				Point2D(0, 200),
			),
			curve.points)
	}
}