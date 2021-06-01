package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
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
 * Performs a logical "XOR" function with the current input signals of a [Vertice].
 */
class XorCalculator<T : Vertice> : VerticeCalculator<T> {

	companion object {
		fun calculate(vertice: Vertice, data: GraphActorData): Bit {
			var trueCount = 0
			var error = false
			var undefined = false

			for (port in vertice.getInputs()) {
				@Suppress("NON_EXHAUSTIVE_WHEN")
				when (data.getSignal<DigitalSignal>(port.portId)!!.bitAt(0)) {
					Bit.True -> trueCount++
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

			return Bit.of(trueCount.rem(2) == 1)
		}
	}

	override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
		vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(calculate(vertice, data)), signalHandler)
	}
}

class XorGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {

	companion object {

		private const val XOR_BASE_RESOURCE_KEY = "library.element.XorGate"
		private val XOR_TYPE get() = Translations.getString("$XOR_BASE_RESOURCE_KEY.name")
		private val XOR_TYPE_DESC get() = Translations.getOptionalString("$XOR_BASE_RESOURCE_KEY.desc")

		private const val ODD_BASE_RESOURCE_KEY = "library.element.OddFunction"
		private val ODD_TYPE get() = Translations.getString("$ODD_BASE_RESOURCE_KEY.name")
		private val ODD_TYPE_DESC get() = Translations.getOptionalString("$ODD_BASE_RESOURCE_KEY.desc")


		val CALCULATOR = XorCalculator<XorGate>()

		val TRUTH_TABLE = TruthTableModel(2, 1)
			.define(intArrayOf(0, 0), 0)
			.define(intArrayOf(0, 1), 1)
			.define(intArrayOf(1, 0), 1)
			.define(intArrayOf(1, 1), 0)
	}

	override val type: String get() = if (inputCount == InputCount.TWO.count) XOR_TYPE else ODD_TYPE
	override val typeDesc: String? get() = if (inputCount == InputCount.TWO.count) XOR_TYPE_DESC else ODD_TYPE_DESC
}
