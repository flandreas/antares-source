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
		assertNull(range.getNext(1.0))
	}

	@Test
	fun shouldSequenceBackwards() {
		val range = PointRange(Point2D(0, 0), Point2D(0, -3))

		assertEquals(3.0, range.size)
		assertEquals(Point2D(0, 0), range.getNext(1.0))
		assertEquals(Point2D(0, -1), range.getNext(1.0))
		assertEquals(Point2D(0, -2), range.getNext(1.0))
		assertEquals(Point2D(0, -3), range.getNext(1.0))
		assertNull(range.getNext(1.0))
	}

	@Test
	fun shouldHandleZeroSize() {
		val range = PointRange(Point2D(10, 10), Point2D(10, 10))

		assertEquals(0.0, range.size)
		assertEquals(Point2D(10, 10), range.getCurrent())
		assertEquals(Point2D(10, 10), range.getNext(1.09))
		assertNull(range.getNext(1.0))
	}

	@Test
	fun shouldAlwaysGetLast() {
		val range = PointRange(Point2D(0, 0), Point2D(0, 3))

		assertEquals(Point2D(0.0, 0.0), range.getNext(1.2))
		assertEquals(Point2D(0.0, 1.2), range.getNext(1.2))
		assertEquals(Point2D(0.0, 2.4), range.getNext(1.2))
		assertEquals(Point2D(0.0, 3.0), range.getNext(1.2))
	}

	@Test
	fun shouldNotReturnLastPointBeyondDistance() {
		val range = PointRange(Point2D(0, 0), Point2D(10, 0), returnEndPoint = false)

		assertEquals(Point2D(0, 0), range.getNext(3.0))
		assertEquals(Point2D(3, 0), range.getNext(3.0))
		assertEquals(Point2D(6, 0), range.getNext(3.0))
		assertEquals(Point2D(9, 0), range.getNext(3.0))
		assertNull(range.getNext(1.0))
	}

	@Test
	fun shouldReturnLastPointAtDistance() {
		val range = PointRange(Point2D(0, 0), Point2D(10, 0), returnEndPoint = false)

		assertEquals(Point2D(0, 0), range.getNext(5.0))
		assertEquals(Point2D(5, 0), range.getNext(5.0))
		assertEquals(Point2D(10, 0), range.getNext(5.0))
		assertNull(range.getNext(1.0))
	}

	@Test
	fun shouldStartWithInitialOffset() {
		val range = PointRange(Point2D(0, 0), Point2D(10, 0), initialOffset = 2.0)

		assertEquals(Point2D(2, 0), range.getNext(3.0))
		assertEquals(Point2D(5, 0), range.getNext(3.0))
		assertEquals(Point2D(8, 0), range.getNext(3.0))
		assertEquals(Point2D(10, 0), range.getNext(3.0))
	}

	@Test
	fun shouldDoForEach() {
		val range = PointRange(Point2D(0, 0), Point2D(10, 0))
		var index = 0

		range.forEach(5.0) { x, y ->
			assertEquals(index * 5.0, x)
			assertEquals(0.0, y)
			index++
		}
		assertEquals(3, index)
	}

	@Test
	fun shouldReturnEndpointWithExceedingDistance() {
		val range = PointRange(Point2D(0, 0), Point2D(10, 0), returnEndPoint = true)

		assertEquals(Point2D(0, 0), range.getNext(20.0))
		assertEquals(Point2D(10, 0), range.getNext(20.0))
		assertNull(range.getNext(20.0))
	}
}