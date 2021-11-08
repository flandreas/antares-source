package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.effectiveGateInputBit
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class TransistorCalculator : VerticeCalculator<Transistor> {
	override fun calculate(vertice: Transistor, data: GraphActorData, signalHandler: SignalHandler) {
		val control = effectiveGateInputBit(data.getSignal<DigitalSignal>(Transistor.GATE_PORT_ID)!!.bitAt(0))
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
) : CalculatingVertice(CALCULATOR) {

	companion object {
		private val DEFAULT_TRANSISTOR_TYPE = TransistorType.N
		private const val BASE_RESOURCE_KEY = "library.element.Transistor"
		private val TYPE_N get() = Translations.getString("$BASE_RESOURCE_KEY.nType.name")
		private val TYPE_N_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.nType.desc")
		private val TYPE_P get() = Translations.getString("$BASE_RESOURCE_KEY.pType.name")
		private val TYPE_P_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.pType.desc")

		private const val SOURCE_PORT_ID = 1
		const val GATE_PORT_ID = 2
		private const val DRAIN_PORT_ID = 3

		private val CALCULATOR = TransistorCalculator()

		private fun logicToType(logic: Logic): TransistorType =
			when (logic) {
				Logic.POSITIVE -> TransistorType.N
				Logic.NEGATIVE -> TransistorType.P
			}
	}

	var bitWidth: BitWidth = bitWidth
		set(value) {
			if (field != value) {
				field = value
				sourcePort.bitWidth = value
				drainPort.bitWidth = value
				stateChanged()
			}
		}

	var transistorType: TransistorType = transistorType
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	init {
		propagationDelay = 10

		addPort(DigitalPortImpl(PortType.INPUT, "S", bitWidth = bitWidth))
		addPort(DigitalPortImpl(PortType.INPUT, "G", bitWidth = bitWidth))
		addPort(DigitalPortImpl(PortType.OUTPUT, "D", bitWidth = bitWidth, canBeUndefined = true))
	}

	/** ---- [GraphElement] interface */

	override val type: String get() =
		when (transistorType) {
			TransistorType.N -> TYPE_N
			TransistorType.P -> TYPE_P
		}

	override val typeDesc: String? get() =
		when (transistorType) {
			TransistorType.N -> TYPE_N_DESC
			TransistorType.P -> TYPE_P_DESC
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("bitWidth", bitWidth.width)
		writer.writeString("type", transistorType.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.of(reader.readInt("bitWidth"))
		if (reader.hasAttribute("logic")) {
			// Backward compatibility: Transistor used to extend TriStateBufferGate
			transistorType = logicToType(Logic.withName(reader.readString("logic")))
		}
		if (reader.hasAttribute("type")) {
			transistorType = TransistorType.withName(reader.readString("type"))
		}
	}

	/** ---- [Actor] */

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay / 2, createActorData(null))
	}

	/** ---- [Transistor] */

	val sourcePort: DigitalPort get() = getPort<DigitalSignal>(SOURCE_PORT_ID) as DigitalPort
	val gatePort: DigitalPort get() = getPort<DigitalSignal>(GATE_PORT_ID) as DigitalPort
	val drainPort: DigitalPort get() = getPort<DigitalSignal>(DRAIN_PORT_ID) as DigitalPort

	val inputPortId: Int get() = SOURCE_PORT_ID

	val outputPortId: Int get() = DRAIN_PORT_ID

	val inputPort: DigitalPort get() = sourcePort
	val outputPort: DigitalPort get() = drainPort

	val isOn: Boolean get() = isOn(effectiveGateInputBit(gatePort.getIncomingSignal()!!.bitAt(0)), this)

	fun isOn(bit: Bit, vertice: Transistor): Boolean =
		when (vertice.transistorType) {
			TransistorType.N -> bit.isSet
			TransistorType.P -> bit.isNotSet
		}
}