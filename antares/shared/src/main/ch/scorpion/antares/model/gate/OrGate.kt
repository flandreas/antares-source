package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.model.truthtable.TruthTableModel
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Performs a logical "OR" function with the current input signals of a [Vertice].
 */
class OrCalculator<T : Vertice> : VerticeCalculator<T> {

	companion object {
		fun calculate(vertice: Vertice, data: GraphActorData): Bit {
			var error = false
			var undefined = false
			for (port in vertice.getInputs()) {
				@Suppress("NON_EXHAUSTIVE_WHEN")
				when (data.getSignal<DigitalSignal>(port.portId)!!.bitAt(0)) {
					Bit.True -> return Bit.True
					Bit.Error -> error = true
					Bit.Undefined -> undefined = true
				}
			}

			if (undefined || error) {
				return Bit.Error
			}

			return Bit.of(false)
		}
	}

	override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
		vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(Companion.calculate(vertice, data)), signalHandler)
	}
}

class OrGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate("library.element.OrGate", CALCULATOR, inputCount) {

	companion object {
		val CALCULATOR = OrCalculator<OrGate>()

		val TRUTH_TABLE = TruthTableModel(2, 1)
			.define(intArrayOf(0, 0), 0)
			.define(intArrayOf(0, 1), 1)
			.define(intArrayOf(1, 0), 1)
			.define(intArrayOf(1, 1), 1)
	}
}
