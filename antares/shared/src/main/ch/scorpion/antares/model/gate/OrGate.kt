package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.model.truthtable.TruthTableModel
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Performs a logical "OR" function with the current input signals of a [Vertice].
 */
class OrCalculator : VerticeCalculator<AbstractDigitalGate> {

	companion object {

		fun calculate(vertice: AbstractDigitalGate, data: GraphActorData): Bit {
			var error = false
			var undefined = false
			for (port in vertice.getInputs().map { it as DigitalPort }) {
				@Suppress("NON_EXHAUSTIVE_WHEN")
				when (port.logic.evaluate(data.getSignal<DigitalSignal>(port.portId)!!.bitAt(0))) {
					Bit.True -> return Bit.True
					Bit.Error -> error = true
					Bit.Undefined -> undefined = true
				}
			}

			if (error) {
				return Bit.Error
			}
			if (undefined) {
				return Bit.False
			}

			return Bit.of(false)
		}
	}

	override fun calculate(vertice: AbstractDigitalGate, data: GraphActorData, signalHandler: SignalHandler) {
		vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(Companion.calculate(vertice, data)), signalHandler)
	}
}

class OrGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.OrGate"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		val CALCULATOR = OrCalculator()

		val TRUTH_TABLE = TruthTableModel(2, 1)
			.define(intArrayOf(0, 0), 0)
			.define(intArrayOf(0, 1), 1)
			.define(intArrayOf(1, 0), 1)
			.define(intArrayOf(1, 1), 1)
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC
}
