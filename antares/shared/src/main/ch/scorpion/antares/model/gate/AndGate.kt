package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.Error
import ch.scorpion.antares.model.signal.Bit.True
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.Vertice

/** Performs a logical "AND" function with the current input signals of a [Vertice].*/
class AndCalculator : AbstractDigitalGateCalculator() {

	override fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit {
		var trueCount = 0
		input.forEach {
			@Suppress("NON_EXHAUSTIVE_WHEN")
			when (it.bitAt(bitIndex)) {
				True -> trueCount++
				Error -> return Error
			}
		}
		return Bit.of(trueCount == input.size)
	}

	override fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit {
		var trueCount = 0
		for (i in 1..input.signalCount) {
			@Suppress("NON_EXHAUSTIVE_WHEN")
			when (effectiveGateInputBit(input.getSignal(i).bitAt(0))) {
				True -> trueCount++
				Error -> return Error
			}
		}
		return Bit.of(trueCount == input.signalCount)
	}
}

/** A digital gate that performs a logical AND operation. */
class AndGate(
	inputCount: InputCount = InputCount.TWO,
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractDigitalGate(CALCULATOR, inputCount, bitWidth) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.AndGate"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		val CALCULATOR = AndCalculator()
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	fun calculate(portFilter: (Int) -> Boolean): DigitalSignal = if (bitWidth.width == BitWidth.BW_1.width) {
		CALCULATOR.calculateSingleBit(this, portFilter)
	} else {
		CALCULATOR.calculateMultiBit(this, portFilter)
	}
}
