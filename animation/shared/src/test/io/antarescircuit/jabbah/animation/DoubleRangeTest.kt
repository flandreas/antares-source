package io.antarescircuit.jabbah.animation

import kotlin.test.*

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
		assertNull(range.getNext(1.0))
	}

	@Test
	fun shouldAlwaysReturnEndWhenGoingForward() {
		val range = DoubleRange(0.0, 2.0)

		assertEquals(0.0, range.getNext(1.5))
		assertEquals(1.5, range.getNext(1.5))
		assertEquals(2.0, range.getNext(1.5))
		assertNull(range.getNext(1.0))
	}

	@Test
	fun shouldGoBackwards() {
		val range = DoubleRange(2.0, 0.0)

		assertEquals(2.0, range.size)
		assertEquals(2.0, range.getNext(1.0))
		assertEquals(1.0, range.getNext(1.0))
		assertEquals(0.0, range.getNext(1.0))
		assertNull(range.getNext(1.0))
	}

	@Test
	fun shouldAlwaysReturnEndWhenGoingBackward() {
		val range = DoubleRange(2.0, 0.0)

		assertEquals(2.0, range.size)
		assertEquals(2.0, range.getNext(1.5))
		assertEquals(0.5, range.getNext(1.5))
		assertEquals(0.0, range.getNext(1.5))
		assertNull(range.getNext(1.0))
	}
}