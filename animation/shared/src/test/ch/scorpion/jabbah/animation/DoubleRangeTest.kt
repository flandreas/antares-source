package ch.scorpion.jabbah.animation

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DoubleRangeTest {

	@BeforeTest
	fun init() {
		AnimationModule.require()
	}

	@Test
	fun shouldGoForward() {
		val range = DoubleRange(0.0, 2.0)

		assertEquals(2.0, range.size)
		assertEquals(0.0, range.getNext(1.0))
		assertEquals(1.0, range.getNext(1.0))
		assertEquals(2.0, range.getNext(1.0))
		assertFalse(range.hasNext())
	}

	@Test
	fun shouldAlwaysReturnEndWhenGoingForward() {
		val range = DoubleRange(0.0, 2.0)

		assertEquals(0.0, range.getNext(1.5))
		assertEquals(1.5, range.getNext(1.5))
		assertEquals(2.0, range.getNext(1.5))
		assertFalse(range.hasNext())
	}

	@Test
	fun shouldGoBackwards() {
		val range = DoubleRange(2.0, 0.0)

		assertEquals(2.0, range.size)
		assertEquals(2.0, range.getNext(1.0))
		assertEquals(1.0, range.getNext(1.0))
		assertEquals(0.0, range.getNext(1.0))
		assertFalse(range.hasNext())
	}

	@Test
	fun shouldAlwaysReturnEndWhenGoingBackward() {
		val range = DoubleRange(2.0, 0.0)

		assertEquals(2.0, range.size)
		assertEquals(2.0, range.getNext(1.5))
		assertEquals(0.5, range.getNext(1.5))
		assertEquals(0.0, range.getNext(1.5))
		assertFalse(range.hasNext())
	}
}