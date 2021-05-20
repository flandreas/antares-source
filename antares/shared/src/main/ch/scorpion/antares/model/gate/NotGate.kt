package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.truthtable.TruthTableModel
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Performs a logical "NOT" function with the current input signal of a [Vertice].
 */
class NotCalculator<T : Vertice> : VerticeCalculator<T> {

    override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
	    vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(
		    data.getSignal<DigitalSignal>(1)?.not(),
		    signalHandler)
    }
}

class NotGate(
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractDigitalGate(CALCULATOR, InputCount.ONE) {

    companion object {
	    private const val BASE_RESOURCE_KEY = "library.element.NotGate"
	    private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
	    private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

	    val CALCULATOR = NotCalculator<NotGate>()

        val TRUTH_TABLE = TruthTableModel(1, 1)
                .define(intArrayOf(0), 1)
                .define(intArrayOf(1), 0)
    }

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	var bitWidth: BitWidth = bitWidth
		set(value) {
			if (field != value) {
				field = value
				(getInput<DigitalSignal>() as DigitalPort).bitWidth = value
				(getOutput<DigitalSignal>() as DigitalPort).bitWidth = value
				stateChanged()
			}
		}

	override fun createOutputPort(): OutputPort<DigitalSignal> {
        return DigitalPortImpl.createOutput(Logic.NEGATIVE)
    }

    override val minInputCount: InputCount get() = InputCount.ONE
    override val maxInputCount: InputCount get() = InputCount.ONE

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (bitWidth != BitWidth.BW_1) {
			writer.writeInt("bitWidth", bitWidth.width)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("bitWidth")) {
			bitWidth = BitWidth.of(reader.readInt("bitWidth"))
		}
	}
}


