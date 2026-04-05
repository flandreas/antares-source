package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.graph.model.MultiSignalSource

/**
 * Performs a NAND calculation by inverting the result of a [AndCalculator].
 */
object NandCalculator : AbstractLogicGateCalculator() {

	override fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit =
		AndCalculator.calculateBit(input, bitIndex).not()

	override fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit =
		AndCalculator.calculateBit(input).not()
}
