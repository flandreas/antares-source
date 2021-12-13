package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthExpression
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Forwards an input signal unchanged to the output.
 */
class BufferCalculator<T : Vertice> : VerticeCalculator<T> {

	override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
		val result = effectiveGateInputWord(data.getSignal(1)!!)
		vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(result, signalHandler)
	}
}

class BufferGate(bitWidth: BitWidth = BitWidth.BW_1) : CalculatingVertice(CALCULATOR) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.Buffer"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		val CALCULATOR = BufferCalculator<BufferGate>()
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

	init {
		propagationDelay = AbstractDigitalGate.DEFAULT_PROPAGATION_DELAY
		addPort(DigitalPortImpl.createInput())
		addPort(DigitalPortImpl.createOutput())
	}

	override fun graphParamsChanged(graph: Graph) {
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay / 2, createActorData(null))
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		bitWidth.write("bitWidth", writer)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("bitWidth")) {
			bitWidth = BitWidth.read("bitWidth", reader)
		}
	}
}


