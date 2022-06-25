package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.Error
import ch.scorpion.antares.model.signal.Bit.True
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * Performs a logical "OR" function with the current input signals of a [Vertice].
 */
class OrCalculator : AbstractDigitalGateCalculator() {

	override fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit {
		var result = false
		input.forEach {
			@Suppress("NON_EXHAUSTIVE_WHEN")
			when (it.bitAt(bitIndex)) {
				True -> result = true
				Error -> return Error
			}
		}
		return Bit.of(result)
	}

	override fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit {
		var result = false
		for (i in 1..input.signalCount) {
			@Suppress("NON_EXHAUSTIVE_WHEN")
			when (effectiveGateInputBit(input.getSignal(i).bitAt(0))) {
				True -> result = true
				Error -> return Error
			}
		}
		return Bit.of(result)
	}
}

class OrGate(
	inputCount: PortCount = PortCount.TWO,
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractDigitalGate(CALCULATOR, inputCount, bitWidth) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.OrGate"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		val CALCULATOR = OrCalculator()
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC
}
