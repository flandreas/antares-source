package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.Error
import ch.scorpion.antares.model.signal.Bit.True
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * Performs a logical "OR" function with the current input signals of a [Vertice].
 */
object OrCalculator : AbstractLogicGateCalculator() {

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
