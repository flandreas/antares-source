package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.model.truthtable.TruthTableModel
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Performs a logical "XNOR" function with the current input signals of a [Vertice].
 */
class XnorCalculator<T : Vertice> : VerticeCalculator<T> {

	companion object {
		fun calculate(vertice: Vertice, data: GraphActorData): Bit {
			return XorCalculator.calculate(vertice, data).not()
		}
	}

	override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
		vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(calculate(vertice, data)), signalHandler)
	}
}

class XnorGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.XnorGate"
		private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		val CALCULATOR = XnorCalculator<XnorGate>()

		val TRUTH_TABLE = TruthTableModel(2, 1)
			.define(intArrayOf(0, 0), 1)
			.define(intArrayOf(0, 1), 0)
			.define(intArrayOf(1, 0), 0)
			.define(intArrayOf(1, 1), 1)
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	override fun createOutputPort(): OutputPort<DigitalSignal> {
		return DigitalPortImpl.createOutput(Logic.NEGATIVE)
	}
}
