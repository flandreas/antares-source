package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.EditTestRule
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [PolylineComponent]. */
class PolylineComponentTest {

	companion object {
		@ClassRule
		@JvmField
		val editTestRule = EditTestRule()
	}

	@Test
	fun shouldMirrorHorizontally() {
		val component = PolylineComponent()
		component.polyline.addPoint(0, 0)
		component.polyline.addPoint(100, 200)

		component.mirrorHorizontally(50.0)

		assertThat(component.polyline.getFirstPoint(), `is`(Point2D(100.0, 0.0)))
		assertThat(component.polyline.getLastPoint(), `is`(Point2D(0.0, 200.0)))
	}

	@Test
	fun shouldMirrorVertically() {
		val component = PolylineComponent()
		component.polyline.addPoint(0, 0)
		component.polyline.addPoint(100, 200)

		component.mirrorVertically(50.0)

		assertThat(component.polyline.getFirstPoint(), `is`(Point2D(0.0, 100.0)))
		assertThat(component.polyline.getLastPoint(), `is`(Point2D(100.0, -100.0)))
	}
}