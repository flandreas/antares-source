package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.graph.model.MultiSignalSource

/**
 * Forwards an input signal unchanged to the output.
 */
object BufferCalculator : AbstractLogicGateCalculator() {

	override fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit =
		calculateOutputBit(input.first().bitAt(bitIndex))

	override fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit =
		calculateOutputBit(input.getSignal(1).bitAt(0))

	private fun calculateOutputBit(inputBit: Bit): Bit =
		when (inputBit) {
			Bit.Undefined -> CurrentUndefinedGateInputBehavior.value.definedBit
			else -> inputBit
		}
}

