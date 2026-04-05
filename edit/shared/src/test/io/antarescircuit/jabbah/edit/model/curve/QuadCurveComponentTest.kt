package io.antarescircuit.jabbah.edit.model.curve

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.drawable.RotationDirection
import io.antarescircuit.jabbah.edit.EditTestRule
import io.antarescircuit.jabbah.edit.select.RectangularRubberBand
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuadCurveComponentTest {

	@BeforeTest
	fun setup() {
		EditTestRule.configure()
	}

	@Test
	fun shouldHaveBoundingBoxWhenBeingFlat() {
		val curve = createFlatCurve()

		assertEquals(Rectangle2D(0, 0, 100, 0), curve.boundingBox)
	}

	@Test
	fun shouldSelectionRubberbandContainFlatCurve() {
		val curve = createFlatCurve()
		val rubberBand = RectangularRubberBand()
		rubberBand.setBounds(-10, -10, 200, 200)

		assertTrue(rubberBand.contains(curve.boundingBox))
	}

	@Test
	fun shouldContainPointWhenBeingFlat() {
		val curve = createFlatCurve()

		assertTrue(curve.contains(Point2D(30, 0)))
	}

	@Test
	fun shouldRotate() {
		val curve = QuadCurveComponent(listOf(
			Point2D(0, 0),
			Point2D(100, -100),
			Point2D(200, 0)))

		curve.rotate(RotationDirection.Clockwise)

		assertEquals(
			listOf(
				Point2D(0, 0),
				Point2D(100, 100),
				Point2D(0, 200),
			),
			curve.points)
	}

	@Test
	fun shouldMirrorHorizontally() {
		val curve = QuadCurveComponent(listOf(
			Point2D(0, 0),
			Point2D(100, -100),
			Point2D(200, 0)))

		curve.mirrorHorizontally(0.0)

		assertEquals(
			listOf(
				Point2D(0, 0),
				Point2D(-100, -100),
				Point2D(-200, 0)
			),
			curve.points)
	}

	@Test
	fun shouldMirrorVertically() {
		val curve = QuadCurveComponent(listOf(
			Point2D(0, 0),
			Point2D(100, -100),
			Point2D(200, 0)))

		curve.mirrorVertically(0.0)

		assertEquals(
			listOf(
				Point2D(0, 0),
				Point2D(100, 100),
				Point2D(200, 0)
			),
			curve.points)
	}

	private fun createFlatCurve(): QuadCurveComponent = QuadCurveComponent(listOf(
		Point2D(0, 0),
		Point2D(100, 0),
		Point2D(50, 0)))
}