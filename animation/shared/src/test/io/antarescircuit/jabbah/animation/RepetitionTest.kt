package io.antarescircuit.jabbah.animation

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RepetitionTest {

	@BeforeTest
	fun init() {
		_root_ide_package_.io.antarescircuit.jabbah.animation.AnimationModule.require()
	}

	@Test
	fun shouldRepeat() {
		val repetition = _root_ide_package_.io.antarescircuit.jabbah.animation.Repetition(
            _root_ide_package_.io.antarescircuit.jabbah.animation.DoubleRange(
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