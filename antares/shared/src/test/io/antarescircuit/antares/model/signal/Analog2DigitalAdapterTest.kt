package io.antarescircuit.antares.model.signal

import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.model.signal.Digital2AnalogAdapter.convertIncomingSignal
import io.antarescircuit.antares.model.signal.Digital2AnalogAdapter.convertOutgoingSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory.of
import kotlin.test.Test
import kotlin.test.assertEquals

class Analog2DigitalAdapterTest {

	@Test
	fun shouldConvertDigitalToAnalog() {
		assertEquals(AnalogSignal.ZERO_VOLTAGE, convertIncomingSignal(of(false)))
		assertEquals(AnalogSignal.HIGH_VOLTAGE, convertIncomingSignal(of(true)))
	}

	@Test
	fun shouldConvertAnalogToDigital() {
		assertEquals(of(false), convertOutgoingSignal(AnalogSignal.ZERO_VOLTAGE))
		assertEquals(of(true), convertOutgoingSignal(AnalogSignal.HIGH_VOLTAGE))
	}

	@Test
	fun shouldConvertMultiBitValuesToZeroVolt() {
		assertEquals(AnalogSignal.ZERO_VOLTAGE, convertIncomingSignal(of(BitWidth.BW_2, 3)))
	}
}