package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.PI_2
import ch.scorpion.jabbah.base.PI_4
import ch.scorpion.jabbah.base.SIGMA
import ch.scorpion.jabbah.base.TWO_PI
import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.math.PI
import kotlin.test.*

/** Unit tests for [Geometry].*/
class GeometryTest {

	@BeforeTest
	fun setup() {
		BaseModule.require()
	}

	@Test
	fun smallDifferenceShouldBeEqual() {
		assertTrue(Geometry.equal(5.0, 5.0 + 0.5 * SIGMA))
		assertTrue(Geometry.equal(0.0, 0.0))
	}

	@Test
	fun largeDifferenceShouldNotBeEqual() {
		assertFalse(Geometry.equal(5.0, 5.0 + 2 * SIGMA))
		assertFalse(Geometry.equal(1.0, 2.0))
	}

	@Test
	fun shouldCalculateAngleLocatedInOrigin() {
		assertEquals(PI_4, Geometry.angle(0.0, 0.0, 10.0, -10.0))
		assertEquals(7 * PI_4, Geometry.angle(0.0, 0.0, 10.0, 10.0))
	}

	@Test
	fun shouldCalculateAngleNotLocatedInOrigin() {
		assertEquals(PI_4, Geometry.angle(100.0, 0.0, 110.0, -10.0))
		assertEquals(7 * PI_4, Geometry.angle(0.0, 100.0, 10.0, 110.0))
	}

	@Test
	fun shouldWrapAngle() {
		assertEquals(0.0, Geometry.wrapAngle(0.0))
		assertEquals(0.0, Geometry.wrapAngle(TWO_PI))
		assertEquals(0.0, Geometry.wrapAngle(2 * TWO_PI))
		assertEquals(PI, Geometry.wrapAngle(1.5 * TWO_PI))
	}

	@Test
	fun shouldWrapNegativeAngle() {
		assertEquals(3 * PI_2, Geometry.wrapAngle(-PI_2))
		assertEquals(3 * PI_2, Geometry.wrapAngle(-TWO_PI - PI_2))
	}

	@Test
	fun shouldBeAntiClockwiseAngleChange() {
		assertFalse(Geometry.isClockwiseAngleChange(0.0, 0.0))
		assertFalse(Geometry.isClockwiseAngleChange(0.0, PI_2))
		assertFalse(Geometry.isClockwiseAngleChange(3 * TWO_PI, PI_2))
	}

	@Test
	fun shouldBeClockwiseAngleChange() {
		assertTrue(Geometry.isClockwiseAngleChange(PI_2, 0.0))
		assertTrue(Geometry.isClockwiseAngleChange(PI_2, 3 * TWO_PI))
	}

	@Test
	fun shouldCalculateSign() {
		assertEquals(0.0, Geometry.sign(0.0))
		assertEquals(0.0, Geometry.sign(0.5 * SIGMA))
		assertEquals(1.0, Geometry.sign(2.3))
		assertEquals(-1.0, Geometry.sign(-5.0))
	}
}