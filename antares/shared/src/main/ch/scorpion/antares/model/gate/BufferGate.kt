package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.model.MultiSignalSource

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

