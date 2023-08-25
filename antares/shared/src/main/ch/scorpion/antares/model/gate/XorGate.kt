package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.Error
import ch.scorpion.antares.model.signal.Bit.True
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * Performs a logical "XOR" function with the current input signals of a [Vertice].
 */
object XorCalculator : AbstractLogicGateCalculator() {

	override fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit {
		var trueCount = 0
		input.forEach {
			@Suppress("NON_EXHAUSTIVE_WHEN")
			when (it.bitAt(bitIndex)) {
				True -> trueCount++
				Error -> return Error
				else -> {}
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