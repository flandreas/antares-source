package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.*

/**
 * Unit tests for [PointRange].
 */
class PointRangeTest {

	@BeforeTest
	fun init() {
		BaseModule.require()
	}

	@Test
	fun shouldSequenceForward() {
		val range = PointRange(Point2D(0, 0), Point2D(0, 3))

		assertEquals(3.0, range.size)
		assertEquals(Point2D(0, 0), range.getNext(1.0))
		assertEquals(Point2D(0, 1), range.getNext(1.0))
		assertEquals(Point2D(0, 2), range.getNext(1.0))
		assertEquals(Point2D(0, 3), range.getNext(1.0))
		assertFalse(range.hasNext())
	}

	@Test
	fun shouldSequenceBackwards() {
		val range = PointRange(Point2D(0, 0), Point2D(0, -3))

		assertEquals(3.0, range.size)
		assertEquals(Point2D(0, 0), range.getNext(1.0))
		assertEquals(Point2D(0, -1), range.getNext(1.0))
		assertEquals(Point2D(0, -2), range.getNext(1.0))
		assertEquals(Point2D(0, -3), range.getNext(1.0))
		assertFalse(range.hasNext())
	}

	@Test
	fun shouldHandleZeroSize() {
		val range = PointRange(Point2D(10, 10), Point2D(10, 10))

		assertEquals(0.0, range.size)
		assertEquals(Point2D(10, 10), range.getCurrent())
		assertTrue(range.hasNext())
		assertEquals(Point2D(10, 10), range.getNext(1.09))
		assertFalse(range.hasNext())
	}

	@Test
	fun shouldAlwaysGetLast() {
		val range = PointRange(Point2D(0, 0), Point2D(0, 3))

		assertEquals(Point2D(0.0, 0.0), range.getNext(1.2))
		assertEquals(Point2D(0.0, 1.2), range.getNext(1.2))
		assertEquals(Point2D(0.0, 2.4), range.getNext(1.2))
		assertEquals(Point2D(0.0, 3.0), range.getNext(1.2))
	}
}