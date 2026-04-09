package io.antarescircuit.jabbah.animation

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RepetitionTest {

	@BeforeTest
	fun init() {
		AnimationModule.require()
	}

	@Test
	fun shouldRepeat() {
		val repetition = Repetition(
            DoubleRange(
                0.0,
                2.0
            )
        )

		assertEquals(0.0, repetition.getNext(1.0))
		assertEquals(1.0, repetition.getNext(1.0))
		assertEquals(2.0, repetition.getNext(1.0))
		assertEquals(0.0, repetition.getNext(1.0))
		assertEquals(1.0, repetition.getNext(1.0))
		assertEquals(2.0, repetition.getNext(1.0))
	}
}