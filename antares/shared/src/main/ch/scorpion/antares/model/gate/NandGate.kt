package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.model.truthtable.TruthTableModel
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Performs a NAND calculation by inverting the result of a [AndCalculator].
 */
class NandCalculator : VerticeCalculator<AbstractDigitalGate> {
	override fun calculate(vertice: AbstractDigitalGate, data: GraphActorData, signalHandler: SignalHandler) {
		vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(AndCalculator.calculate(vertice, data).not()), signalHandler)
	}
}

class NandGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.NandGate"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		val CALCULATOR = NandCalculator()

		val TRUTH_TABLE = TruthTableModel(2, 1)
			.define(intArrayOf(0, 0), 1)
			.define(intArrayOf(0, 1), 1)
			.define(intArrayOf(1, 0), 1)
			.define(intArrayOf(1, 1), 0)
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	override fun createOutputPort(): OutputPort<DigitalSignal> {
		return DigitalPortImpl.createOutput(Logic.NEGATIVE)
	}
}
