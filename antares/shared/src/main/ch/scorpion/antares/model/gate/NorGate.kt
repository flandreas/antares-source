package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * Performs a logical "NOR" function with the current input signals of a [Vertice].
 */
object NorCalculator : AbstractLogicGateCalculator() {
	override fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit =
		OrCalculator.calculateBit(input, bitIndex).not()

	override fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit =
		OrCalculator.calculateBit(input).not()
}

