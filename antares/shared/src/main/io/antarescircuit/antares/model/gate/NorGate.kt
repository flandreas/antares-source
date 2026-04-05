package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.graph.model.MultiSignalSource
import io.antarescircuit.jabbah.graph.model.Vertice

/**
 * Performs a logical "NOR" function with the current input signals of a [Vertice].
 */
object NorCalculator : AbstractLogicGateCalculator() {
	override fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit =
		OrCalculator.calculateBit(input, bitIndex).not()

	override fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit =
		OrCalculator.calculateBit(input).not()
}

