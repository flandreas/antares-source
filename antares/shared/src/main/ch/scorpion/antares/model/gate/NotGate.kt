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
 * Performs a logical "NOT" function with the current input signal of a [Vertice].
 */
class NotCalculator<T : Vertice> : VerticeCalculator<T> {

    override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
        val outputPort = vertice.getOutput<DigitalSignal>()
        val signal = data.getSignal<DigitalSignal>(1)!!

	    val result = when (val bit = signal.bitAt(0)) {
            Bit.Error -> Bit.Error
            Bit.Undefined -> Bit.Undefined
            else -> bit.not()
        }
        outputPort.setOutgoingSignalBuffered(Word.of(result), signalHandler)
    }
}

class NotGate : AbstractDigitalGate(CALCULATOR, InputCount.ONE) {

    companion object {
	    private const val BASE_RESOURCE_KEY = "library.element.NotGate"
	    private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
	    private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

	    val CALCULATOR = NotCalculator<NotGate>()

        val TRUTH_TABLE = TruthTableModel(1, 1)
                .define(intArrayOf(0), 1)
                .define(intArrayOf(1), 0)
    }

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	override fun createOutputPort(): OutputPort<DigitalSignal> {
        return DigitalPortImpl.createOutput(Logic.NEGATIVE)
    }

    override val minInputCount: InputCount get() = InputCount.ONE
    override val maxInputCount: InputCount get() = InputCount.ONE
}


