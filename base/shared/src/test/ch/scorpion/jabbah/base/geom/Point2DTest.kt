package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.math.abs
import kotlin.test.*

/**
 * Unit tests for [Point2DTest].
 */
class Point2DTest {

	@BeforeTest
	fun init() {
		BaseModule.require()
	}

	@Test
	fun shouldBeEqualOutsideSigma() {
		assertEquals(Point2D(0.0, 0.0), Point2D(0.000000001, 0.000000001))
	}

	@Test
	fun shouldBeDifferentInsideSigma() {
		assertNotEquals(Point2D(0.0, 0.0), Point2D(0.1, 0.0))
	}

	@Test
	fun shouldAccessCoordinates() {
		assertEquals(10.0, Point2D(10.0, 20.0).x)
		assertEquals(20.0, Point2D(10.0, 20.0).y)
	}

	@Test
	fun shouldConstructDefault() {
		assertEquals(Point2D.ZERO, Point2D(0.0, 0.0))
	}

	@Test
	fun shouldCalculateDistanceSq() {
		assertTrue(abs(25 - Point2D(0.0, 0.0).distanceSq(3.0, 4.0)) < 0.1)
	}

	@Test
	fun shouldCalculateDistance() {
		assertTrue(abs(5.0 - Point2D(0.0, 0.0).distance(3.0, 4.0)) < 0.1)
	}

	@Test
	fun shouldConvertToString() {
		assertEquals("Point2D(10.0,10.0)", Point2D(10.0, 10.0).toString())
	}

	@Test
	fun shouldMirrorHorizontally() {
		assertEquals(Point2D(-10, 20), Point2D(10, 20).mirrorHorizontally(0.0))
	}

	@Test
	fun shouldMirrorVertically() {
		assertEquals(Point2D(10, -20), Point2D(10, 20).mirrorVertically(0.0))
	}
}