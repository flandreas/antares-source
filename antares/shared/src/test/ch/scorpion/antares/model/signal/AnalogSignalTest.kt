package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.analog.AnalogSignal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnalogSignalTest {

	@Test
	fun shouldBeComparable() {
		assertTrue(AnalogSignal(5.0) > AnalogSignal(0.0))
		assertFalse(AnalogSignal(3.0) < AnalogSignal(2.0))
		assertEquals(AnalogSignal(1.8), AnalogSignal(1.8))
	}
}