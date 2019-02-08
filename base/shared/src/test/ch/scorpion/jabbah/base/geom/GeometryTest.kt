package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.MathClass
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Unit tests for [Geometry].*/
class GeometryTest {

	@BeforeTest
	fun setup() {
		BaseModuleJvm.require()
	}

	@Test
	fun shouldCalculateAngleLocatedInOrigin() {
		assertEquals(MathClass.PI_4, Geometry.angle(0.0, 0.0, 10.0, -10.0))
		assertEquals(7 * MathClass.PI_4, Geometry.angle(0.0, 0.0, 10.0, 10.0))
	}

	@Test
	fun shouldCalculateAngleNotLocatedInOrigin() {
		assertEquals(MathClass.PI_4, Geometry.angle(100.0, 0.0, 110.0, -10.0))
		assertEquals(7 * MathClass.PI_4, Geometry.angle(0.0, 100.0, 10.0, 110.0))
	}

	@Test
	fun shouldWrapAngle() {
		assertEquals(0.0, Geometry.wrapAngle(0.0))
		assertEquals(0.0, Geometry.wrapAngle(MathClass.TWO_PI))
		assertEquals(0.0, Geometry.wrapAngle(2 * MathClass.TWO_PI))
		assertEquals(MathClass.PI, Geometry.wrapAngle(1.5 * MathClass.TWO_PI))
	}

	@Test
	fun shouldWrapNegativeAngle() {
		assertEquals(3 * MathClass.PI_2, Geometry.wrapAngle(-MathClass.PI_2))
		assertEquals(3 * MathClass.PI_2, Geometry.wrapAngle(-MathClass.TWO_PI - MathClass.PI_2))
	}

	@Test
	fun shouldBeAntiClockwiseAngleChange() {
		assertFalse(Geometry.isClockwiseAngleChange(0.0, 0.0))
		assertFalse(Geometry.isClockwiseAngleChange(0.0, MathClass.PI_2))
		assertFalse(Geometry.isClockwiseAngleChange(3 * MathClass.TWO_PI, MathClass.PI_2))
	}

	@Test
	fun shouldBeClockwiseAngleChange() {
		assertTrue(Geometry.isClockwiseAngleChange(MathClass.PI_2, 0.0))
		assertTrue(Geometry.isClockwiseAngleChange(MathClass.PI_2, 3 * MathClass.TWO_PI))
	}
}