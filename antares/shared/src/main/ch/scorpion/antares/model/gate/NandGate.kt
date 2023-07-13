package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.model.MultiSignalSource

/**
 * Performs a NAND calculation by inverting the result of a [AndCalculator].
 */
object NandCalculator : AbstractLogicGateCalculator() {

	override fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit =
		AndCalculator.calculateBit(input, bitIndex).not()

	override fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit =
		AndCalculator.calculateBit(input).not()
}
