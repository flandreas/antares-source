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