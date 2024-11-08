package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.effectiveGateInputBit
import ch.scorpion.antares.model.net.TransistorIF.Companion.DEFAULT_TRANSISTOR_TYPE
import ch.scorpion.antares.model.net.TransistorIF.Companion.DRAIN_PORT_ID
import ch.scorpion.antares.model.net.TransistorIF.Companion.GATE_PORT_ID
import ch.scorpion.antares.model.net.TransistorIF.Companion.SOURCE_PORT_ID
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class TransistorCalculator : VerticeCalculator<Transistor> {
	override fun calculate(vertice: Transistor, data: GraphActorData, signalHandler: SignalHandler) {
		val control = effectiveGateInputBit(data.getSignal<DigitalSignal>(GATE_PORT_ID)!!.bitAt(0))
		val result = when (control) {
			Bit.Error -> DigitalSignalFactory.error(vertice.bitWidth)
			else -> {
				if (vertice.isOn(control, vertice)) {
					calculateOutputValue(data.getSignal(vertice.inputPortId)!!)
				} else {
					DigitalSignalFactory.undefined(vertice.bitWidth)
				}
			}
		}
		vertice.outputPort.setOutgoingSignalBuffered(result, signalHandler)
	}

	private fun calculateOutputValue(inputValue: DigitalSignal): DigitalSignal {
		return DigitalSignalFactory.ofBits(inputValue.bits.map {
			when (it) {
				Bit.Undefined -> Bit.Undefined
				else -> it
			}
		})
	}
}

/**
 * [Transistor] implements a MOSFET transistor with a gate [Port] controlling whether signals
 * can flow along the other two [Ports][Port], depending on [TransistorType].
 *
 * When [transistorType] gets changed, the ID and names of the [Ports][Port] remain the same,
 * but there [PortType] changes: For [TransistorType.N], the source [Port] is an input,
 * while for [TransistorType.P], the source [Port] is an output (and vice versa for the drain [Port]).
 */
class Transistor(
	transistorType: TransistorType = DEFAULT_TRANSISTOR_TYPE,
	bitWidth: BitWidth = BitWidth.BW_1
) : CalculatingVertice(CALCULATOR), TransistorIF<DigitalSignal> {

	companion object {
		private val CALCULATOR = TransistorCalculator()

		private fun logicToType(logic: Logic): TransistorType =
			when (logic) {
				Logic.POSITIVE -> TransistorType.N
				Logic.NEGATIVE -> TransistorType.P
			}

		private val GATE_DESC by lazy { TranslatableText(Translation.ofStaticKey("library.element.Transistor.gate.desc"))  }
		private val SOURCE_DESC by lazy { TranslatableText(Translation.ofStaticKey("library.element.Transistor.source.desc"))  }
		private val DRAIN_DESC by lazy { TranslatableText(Translation.ofStaticKey("library.element.Transistor.drain.desc"))  }
	}

	override val baseResourceKey: String get() = "library.element.Transistor"

	var bitWidth: BitWidth = bitWidth
		set(value) {
			if (field != value) {
				field = value
				(sourcePort as DigitalPort).bitWidth = value
				(drainPort as DigitalPort).bitWidth = value
				stateChanged()
			}
		}

	override var transistorType: TransistorType = transistorType
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	init {
		propagationDelay = LongValueImpl(10)

		addPort(DigitalPortImpl(PortType.INPUT, "S", bitWidth = bitWidth, description = SOURCE_DESC))
		addPort(DigitalPortImpl(PortType.INPUT, "G", bitWidth = bitWidth, description = GATE_DESC))
		addPort(DigitalPortImpl(PortType.OUTPUT, "D", bitWidth = bitWidth, description = DRAIN_DESC, canBeUndefined = true))
	}

	/** ---- [GraphElement] interface */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super<CalculatingVertice>.write(writer)
		super<TransistorIF>.write(writer)
		bitWidth.write("bitWidth", writer)
	}

	override fun read(reader: StoreReader) {
		super<CalculatingVertice>.read(reader)
		super<TransistorIF>.read(reader)
		bitWidth = BitWidth.read("bitWidth", reader)
		if (reader.hasAttribute("logic")) {
			// Backward compatibility: Transistor used to extend TriStateBufferGate
			transistorType = logicToType(Logic.withName(reader.readString("logic")))
		}
	}

	/** ---- [Actor] */

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay.value / 2, createActorData(null))
	}

	/** ---- [Transistor] */

	val inputPortId: Int get() = SOURCE_PORT_ID

	val outputPortId: Int get() = DRAIN_PORT_ID

	val inputPort: DigitalPort get() = sourcePort as DigitalPort
	val outputPort: DigitalPort get() = drainPort as DigitalPort

	override val isOn: Boolean get() = isOn(effectiveGateInputBit((gatePort as DigitalPort).getIncomingSignal()!!.bitAt(0)), this)

	fun isOn(bit: Bit, vertice: Transistor): Boolean =
		when (vertice.transistorType) {
			TransistorType.N -> bit.isSet
			TransistorType.P -> bit.isNotSet
		}
}