package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.jabbah.graph.model.GraphTypeSignalAdapter

object Digital2AnalogAdapter : GraphTypeSignalAdapter<AnalogSignal, DigitalSignal> {

	private const val MIN_HIGH_VOLTAGE = 2.0

	override fun convertIncomingSignal(signal: DigitalSignal?): AnalogSignal {
		if (signal == null) {
			return AnalogSignal.ZERO
		}
		if (signal.bitWidth.width > 1) {
			return AnalogSignal.ZERO
		}
		return if (signal.isAllOf(Bit.True)) {
			AnalogSignal.HIGH
		} else {
			AnalogSignal.ZERO
		}
	}

	override fun convertOutgoingSignal(signal: AnalogSignal?): DigitalSignal {
		if (signal == null) {
			return DigitalSignalFactory.undefined(BitWidth.BW_1)
		}
		return DigitalSignalFactory.of(signal.voltage >= MIN_HIGH_VOLTAGE)
	}
}

object Digital2DigitalAdapter : GraphTypeSignalAdapter<DigitalSignal, DigitalSignal> {
	override fun convertIncomingSignal(signal: DigitalSignal?): DigitalSignal? = signal
	override fun convertOutgoingSignal(signal: DigitalSignal?): DigitalSignal? = signal
}
