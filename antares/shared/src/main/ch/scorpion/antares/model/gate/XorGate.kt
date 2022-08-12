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
 * Performs a logical "XOR" function with the current input signals of a [Vertice].
 */
class XorCalculator : AbstractDigitalGateCalculator() {

	override fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit {
		var trueCount = 0
		input.forEach {
			@Suppress("NON_EXHAUSTIVE_WHEN")
			when (it.bitAt(bitIndex)) {
				True -> trueCount++
				Error -> return Error
			}
		}
		return Bit.of(trueCount.rem(2) == 1)
	}

	override fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit {
		var trueCount = 0
		for (i in 1..input.signalCount) {
			when (effectiveGateInputBit(input.getSignal(i).bitAt(0))) {
				True -> trueCount++
				Error -> return Error
				else -> {}
			}
		}
		return Bit.of(trueCount.rem(2) == 1)
	}
}

class XorGate(
	inputCount: PortCount = PortCount.TWO,
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractDigitalGate(CALCULATOR, inputCount, bitWidth) {

	companion object {

		private const val XOR_BASE_RESOURCE_KEY = "library.element.XorGate"
		private val XOR_TYPE get() = Translations.getString("$XOR_BASE_RESOURCE_KEY.name")
		private val XOR_TYPE_DESC get() = Translations.getOptionalString("$XOR_BASE_RESOURCE_KEY.desc")

		private const val ODD_BASE_RESOURCE_KEY = "library.element.OddFunction"
		private val ODD_TYPE get() = Translations.getString("$ODD_BASE_RESOURCE_KEY.name")
		private val ODD_TYPE_DESC get() = Translations.getOptionalString("$ODD_BASE_RESOURCE_KEY.desc")


		val CALCULATOR = XorCalculator()
	}

	override val type: String get() = if (inputCount == PortCount.TWO.count) XOR_TYPE else ODD_TYPE
	override val typeDesc: String? get() = if (inputCount == PortCount.TWO.count) XOR_TYPE_DESC else ODD_TYPE_DESC
}
