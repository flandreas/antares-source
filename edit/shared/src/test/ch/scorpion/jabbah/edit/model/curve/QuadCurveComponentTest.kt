package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.select.RectangularRubberBand
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

	private fun createFlatCurve(): QuadCurveComponent = QuadCurveComponent(listOf(
		Point2D(0, 0),
		Point2D(100, 0),
		Point2D(50, 0)))
}