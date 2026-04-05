package io.antarescircuit.jabbah.animation

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OscillationTest {

	@BeforeTest
	fun init() {
		_root_ide_package_.io.antarescircuit.jabbah.animation.AnimationModule.require()
	}

	@Test
	fun shouldOscillate() {
		val oscillation = _root_ide_package_.io.antarescircuit.jabbah.animation.Oscillation(
            _root_ide_package_.io.antarescircuit.jabbah.animation.DoubleRange(
                0.0,
                2.0
            )
        )

		assertEquals(0.0, oscillation.getNext(1.0))
		assertEquals(1.0, oscillation.getNext(1.0))
		assertEquals(2.0, oscillation.getNext(1.0))
		assertEquals(2.0, oscillation.getNext(1.0))
		assertEquals(1.0, oscillation.getNext(1.0))
		assertEquals(0.0, oscillation.getNext(1.0))
		assertEquals(0.0, oscillation.getNext(1.0))
		assertEquals(1.0, oscillation.getNext(1.0))
	}
}