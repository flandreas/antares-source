package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.model.signal.Digital2AnalogAdapter.convertIncomingSignal
import ch.scorpion.antares.model.signal.Digital2AnalogAdapter.convertOutgoingSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory.of
import kotlin.test.Test
import kotlin.test.assertEquals

class Analog2DigitalAdapterTest {

	@Test
	fun shouldConvertDigitalToAnalog() {
		assertEquals(AnalogSignal.ZERO, convertIncomingSignal(of(false)))
		assertEquals(AnalogSignal(5.0), convertIncomingSignal(of(true)))
	}

	@Test
	fun shouldConvertAnalogToDigital() {
		assertEquals(of(false), convertOutgoingSignal(AnalogSignal(0.0)))
		assertEquals(of(true), convertOutgoingSignal(AnalogSignal(5.0)))
	}

	@Test
	fun shouldConvertMultiBitValuesToZeroVolt() {
		assertEquals(AnalogSignal.ZERO, convertIncomingSignal(of(BitWidth.BW_2, 3)))
	}
}