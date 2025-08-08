package ch.scorpion.antares.model.arithmetic

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.AbstractLogicGate
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.vertice.AdjustableBitWidth
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Extends an input signal of [inputBitWidth] to an output signal of [outputBitWidth] while preserving
 * the sign, i.e. by filling the wider output signal with the input signal's most significant [Bit].
 * [outputBitWidth] must be larger than [inputBitWidth].
 */
class BitExtender(
	inputBitWidth: BitWidth = BitWidth.BW_1,
	outputBitWidth: BitWidth = BitWidth.BW_8
) : CalculatingVertice(CALCULATOR), AdjustableBitWidth {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.BitExtender"
		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<BitExtender> {
			override fun calculate(vertice: BitExtender, data: GraphActorData, signalHandler: SignalHandler) {
				val input = vertice.digitalInput.getIncomingSignal()!!
				val inputMsb = input.msb
				val outputBits = mutableListOf<Bit>()
				for (bit in input.bits) {
					outputBits.add(bit)
				}
				for (i in input.bitWidth.width until vertice.outputBitWidth.width) {
					outputBits.add(inputMsb)
				}
				vertice.digitalOutput.setOutgoingSignalBuffered(DigitalSignalFactory.ofBits(outputBits), signalHandler)
			}
		}
	}

	override val type: String get() = Translations.getString("$BASE_RESOURCE_KEY.name")
	override val typeDesc: String get() = Translations.getString("$BASE_RESOURCE_KEY.desc")

	val digitalInput: DigitalPort get() = getInput<DigitalSignal>() as DigitalPort
	val digitalOutput: DigitalPort get() = getOutput<DigitalSignal>() as DigitalPort

	init {
		propagationDelay = AbstractLogicGate.DEFAULT_PROPAGATION_DELAY
		addPort(DigitalPortImpl.createInput(logic = Logic.POSITIVE, name = null, bitWidth = inputBitWidth))
		addPort(DigitalPortImpl.createOutput(logic = Logic.POSITIVE, name = null,  bitWidth = outputBitWidth))
	}

	var inputBitWidth: BitWidth
		get() = digitalInput.bitWidth
		set(value) {
			if (value != inputBitWidth) {
				require(value.width <= outputBitWidth.width) { "Input BitWidth must be smaller than or equal to output" }
				digitalInput.bitWidth = value
				stateChanged()
			}
		}

	var outputBitWidth: BitWidth
		get() = digitalOutput.bitWidth
		set(value) {
			if (value != outputBitWidth) {
				require(value.width >= inputBitWidth.width) { "Output BitWidth must be larger than or equal that input" }
				digitalOutput.bitWidth = value
				stateChanged()
			}
		}

	/** ---- [Actor] interface */

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay.value / 2, createActorData(null))
	}

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(inputBitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> inputBitWidth = bw } }
		(outputBitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> outputBitWidth = bw } }
	}

	/** ---- [AdjustableBitWidth] */

	override fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean {
		val port = getPort<DigitalSignal>(portId)
		if (port === digitalInput) {
			if (bitWidth.width <= outputBitWidth.width) {
				inputBitWidth = bitWidth
				return true
			}
		} else if (port === digitalOutput) {
			if (bitWidth.width >= inputBitWidth.width) {
				outputBitWidth = bitWidth
				return true
			}
		}
		return false
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		inputBitWidth.write("inputBitWidth", writer)
		outputBitWidth.write("outputBitWidth", writer)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		digitalInput.bitWidth = BitWidth.read("inputBitWidth", reader)
		digitalOutput.bitWidth = BitWidth.read("outputBitWidth", reader)
		stateChanged()
	}
}