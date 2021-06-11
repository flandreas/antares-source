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
class XnorCalculator : VerticeCalculator<AbstractDigitalGate> {

	companion object {
		fun calculate(vertice: AbstractDigitalGate, data: GraphActorData): Bit =
			XorCalculator.calculate(vertice, data).not()
	}

	override fun calculate(vertice: AbstractDigitalGate, data: GraphActorData, signalHandler: SignalHandler) {
		vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(calculate(vertice, data)), signalHandler)
	}
}

class XnorGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {

	companion object {

		private const val XNOR_BASE_RESOURCE_KEY = "library.element.XnorGate"
		private val XNOR_TYPE get() = Translations.getString("$XNOR_BASE_RESOURCE_KEY.name")
		private val XNOR_TYPE_DESC get() = Translations.getOptionalString("$XNOR_BASE_RESOURCE_KEY.desc")

		private const val EVEN_BASE_RESOURCE_KEY = "library.element.EvenFunction"
		private val EVEN_TYPE get() = Translations.getString("$EVEN_BASE_RESOURCE_KEY.name")
		private val EVEN_TYPE_DESC get() = Translations.getOptionalString("$EVEN_BASE_RESOURCE_KEY.desc")


		val CALCULATOR = XnorCalculator()

		val TRUTH_TABLE = TruthTableModel(2, 1)
			.define(intArrayOf(0, 0), 1)
			.define(intArrayOf(0, 1), 0)
			.define(intArrayOf(1, 0), 0)
			.define(intArrayOf(1, 1), 1)
	}

	override val type: String get() = if (inputCount == InputCount.TWO.count) XNOR_TYPE else EVEN_TYPE
	override val typeDesc: String? get() = if (inputCount == InputCount.TWO.count) XNOR_TYPE_DESC else EVEN_TYPE_DESC

	override fun createOutputPort(): OutputPort<DigitalSignal> = DigitalPortImpl.createOutput(Logic.NEGATIVE)
}
