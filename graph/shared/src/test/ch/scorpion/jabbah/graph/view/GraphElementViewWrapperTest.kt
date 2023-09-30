package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.curve.QuadCurveComponent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraphElementViewWrapperTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
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