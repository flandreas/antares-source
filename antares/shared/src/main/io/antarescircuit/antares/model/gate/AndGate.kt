package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.Bit.Error
import io.antarescircuit.antares.model.signal.Bit.True
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.graph.model.MultiSignalSource
import io.antarescircuit.jabbah.graph.model.Vertice

/** Performs a logical "AND" function with the current input signals of a [Vertice].*/
object AndCalculator : AbstractLogicGateCalculator() {

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
		return Bit.of(trueCount == input.size)
	}

	override fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit {
		var trueCount = 0
		for (i in 1..input.signalCount) {
			@Suppress("NON_EXHAUSTIVE_WHEN")
			when (effectiveGateInputBit(input.getSignal(i).bitAt(0))) {
				True -> trueCount++
				Error -> return Error
				else -> {}
			}
		}
		return Bit.of(trueCount == input.signalCount)
	}
}