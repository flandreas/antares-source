package ch.scorpion.antares.model.inout

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.SignalUtil.differ
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.vertice.AbstractGraphPort
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


/**
 * A standard implementation of the [CircuitInOut] interface.
 */
class CircuitInOutImpl(
	val eventBus: EventBus = BaseModule.eventBus,
	name: String? = null,
	portType: PortType = PortType.INPUT,
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractGraphPort<DigitalSignal>(
	port = DigitalPortImpl(portType.reverse(), bitWidth = bitWidth, canBeUndefined = portType == PortType.INOUT),
	name = name,
	calculator = CALCULATOR
), CircuitInOut {

	companion object {

		private const val INPUT_BASE_RESOURCE_KEY = "library.element.GraphInput"
		private val INPUT_TYPE get() = Translations.getString("$INPUT_BASE_RESOURCE_KEY.name")
		private val INPUT_TYPE_DESC get() = Translations.getOptionalString("$INPUT_BASE_RESOURCE_KEY.desc")

		private const val OUTPUT_BASE_RESOURCE_KEY = "library.element.GraphOutput"
		private val OUTPUT_TYPE get() = Translations.getString("$OUTPUT_BASE_RESOURCE_KEY.name")
		private val OUTPUT_TYPE_DESC get() = Translations.getOptionalString("$OUTPUT_BASE_RESOURCE_KEY.desc")

		private const val INOUT_BASE_RESOURCE_KEY = "library.element.GraphInOut"
		private val INOUT_TYPE get() = Translations.getString("$INOUT_BASE_RESOURCE_KEY.name")
		private val INOUT_TYPE_DESC get() = Translations.getOptionalString("$INOUT_BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<CircuitInOutImpl> {
			override fun calculate(vertice: CircuitInOutImpl, data: GraphActorData, signalHandler: SignalHandler) {
				with(vertice) {
					setOutgoingSignal(data.getSignal(1)!!, signalHandler, data.changedPort == null)
					setInteractionEnabled(true, signalHandler)
				}
			}
		}
	}

	/** ---- [GraphElement] */

	override val type: String
		get() = when (portType) {
			PortType.INOUT -> INOUT_TYPE
			PortType.INPUT -> INPUT_TYPE
			PortType.OUTPUT -> OUTPUT_TYPE
		}

	override val typeDesc: String?
		get() = when (portType) {
			PortType.INOUT -> INOUT_TYPE_DESC
			PortType.INPUT -> INPUT_TYPE_DESC
			PortType.OUTPUT -> OUTPUT_TYPE_DESC
		}

	override fun graphParamsChanged(graph: Graph) {
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	/** ---- [GraphPort] interface */

	/** Captures the [DigitalSignal] that has been process last, either as input or as output. */
	override var signal: DigitalSignal? = null
		get() = field ?: DigitalSignalFactory.undefined(bitWidth)

	override var portType: PortType
		get() = getDigitalPort().portType.reverse()
		set(value) {
			if (portType != value) {
				val oldValue = portType
				getDigitalPort().portType = value.reverse()
				eventBus.post(GraphPortTypeChanged(this, oldValue, portType))
			}
		}

	/** ---- [GraphInput] interface */

	override var subGraphInputPort: SubGraphInputPort<DigitalSignal>? = null

	override fun setIncomingSignal(signal: DigitalSignal?, signalHandler: SignalHandler, force: Boolean) {
		setIncomingSignal(signal, signalHandler, propagationDelay, force)
	}

	override fun outputChanged(output: OutputPort<*>, signalHandler: SignalHandler) {
		signal = getDigitalPort().getOutgoingSignal()
		super.outputChanged(output, signalHandler)
	}

	private fun setIncomingSignal(signal: DigitalSignal?, signalHandler: SignalHandler, delay: Long, force: Boolean = false) {
		val differs = differ(this.signal, signal)
		signalHandler.logActorTrace(this) { "GraphInput.setIncomingSignal: enabled=$enabled, differs=$differs" }
		if (differs) {
			this.signal = signal
			setInteractionEnabled(false, signalHandler)
			requestActingAfter(signalHandler, delay, StoringGraphActorData(null, this.signal, force = force))
		}
	}

	/** ---- [GraphOutput] */

	override var subGraphOutputPort: SubGraphOutputPort<DigitalSignal>? = null

	/** ---- [NetCombiner] interface */

	/** Only required for toplevel [CircuitInOutImpl] that can produce a signal when the user clicks on it. */
	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean =
		portType.isInput && subGraphInputPort == null // Clickable input in top-level Graph
			|| portType == PortType.OUTPUT && subGraphOutputPort != null

	override fun <T : Any> createCombinedNetsFor(outputPort: OutputPort<T>, inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<CombinedNet<T>> =
		createCombinedNetsForOutput(outputPort as OutputPort<DigitalSignal>, signalHandler) as Collection<CombinedNet<T>>

	override fun formNet(signalHandler: SignalHandler) {
		if (portType == PortType.OUTPUT && subGraphOutputPort != null) {
			subGraphOutputPort!!.formNet(signalHandler)
		} else {
			super.formNet(signalHandler)
		}
	}

	private fun createCombinedNetsForOutput(outputPort: OutputPort<DigitalSignal>, signalHandler: SignalHandler): Collection<CombinedNet<DigitalSignal>> {
		if (subGraphOutputPort == null) {
			return emptyList()
		}

		if (portType.isInput) {
			return CombinedNet.createFor(subGraphOutputPort!!, signalHandler).onEach {
				it.replaceAccessPort(subGraphOutputPort as OutputPort<DigitalSignal>, outputPort)
			}
		}

		return emptyList()
	}

	/** ---- [Vertice] */

	override fun <T : Any> replaceUndefinedOutput(signal: T?) {
		if (signal is DigitalSignal?) {
			signal?.let { this.signal = it }
		}
	}

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		setInteractionEnabled(true, signalHandler)
		signal = getDigitalPort().dominantSignal
		stateChanged(signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay, StoringGraphActorData(null, signal))
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		resetExecutionState(signalHandler)
	}

	override fun act(signalHandler: SignalHandler, data: ActorData) {
		super.act(signalHandler, data)

		if (portType.isOutput && subGraphOutputPort != null) {
			if ((data as GraphActorData).changedPort != null || signalHandler.executionTime == propagationDelay) {
				// Send signal to outside only if it came from inside
				subGraphOutputPort?.flush(signalHandler, data.force)
			}
		}
	}

	private fun resetExecutionState(signalHandler: SignalHandler) {
		signal = getDigitalPort().defaultDigitalSignal
		if (portType.isInput) {
			getOutput<DigitalSignal>().setOutgoingSignalBuffered(signal, signalHandler)
		}
		stateChanged(signalHandler)
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("type", portType.customName)
		bitWidth.write("bitWidth", writer)
		/*
		if (canBeUndefined) {
			writer.writeBoolean("canBeUndefined", canBeUndefined)
		}
		*/
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		portType = PortType.withName(reader.readString("type"))
		bitWidth = BitWidth.read("bitWidth", reader)
		/*
		if (reader.hasAttribute("canBeUndefined")) {
			canBeUndefined = reader.readBoolean("canBeUndefined")
		}
		*/
	}

	/** ---- [CircuitInOut] interface */

	override var signalRepresentation: DigitalSignalRepresentation
		get() = getDigitalPort().signalRepresentation
		set(value) {
			if (value != getDigitalPort().signalRepresentation) {
				val oldValue = getDigitalPort().signalRepresentation
				getDigitalPort().signalRepresentation = value
				if (isNotReading) {
					eventBus.post(CircuitInOutSignalRepresentationChanged(this, oldValue, value))
				}
			}
		}

	override val isToplevel: Boolean get() = subGraphInputPort == null

	override var bitWidth: BitWidth
		get() = getDigitalPort().bitWidth
		set(value) {
			if (value != getDigitalPort().bitWidth) {
				signal = null
				val oldValue = getDigitalPort().bitWidth
				getDigitalPort().bitWidth = value
				if (isNotReading) {
					stateChanged(null)
					eventBus.post(CircuitInOutBitWidthChanged(this, oldValue, value))
				}
			}
		}

	override fun toggleBit(index: Int, undefine: Boolean, signalHandler: SignalHandler) {
		var s = signal
		if (s == null) {
			s = DigitalSignalFactory.allOf(bitWidth, Bit.Undefined)
		}
		var bit = s.bitAt(index)
		if (undefine) {
			bit = Bit.Undefined
		} else {
			if (!bit.isDefined) {
				bit = Bit.False
			}
		}
		setIncomingSignal(s.withBit(index, bit.not()), signalHandler, Switch.DEF_PROP_DELAY)
	}

	override fun setSignalManually(signal: DigitalSignal, signalHandler: SignalHandler) {
		setIncomingSignal(signal, signalHandler, Switch.DEF_PROP_DELAY)
	}

	/** ---- [CircuitInOutImpl] */

	private fun getDigitalPort(): DigitalPort = getPort<DigitalPort>() as DigitalPort

	private fun setOutgoingSignal(signal: DigitalSignal, signalHandler: SignalHandler, fromOutside: Boolean) {
		this.signal = signal
		stateChanged(signalHandler)

		if (signalHandler.executionTime == propagationDelay) {
			// start-up
			if (portType.isOutput) {
				propagateToSubGraphOutputPort(signal, signalHandler)
			}
			if (portType.isInput) {
				getOutput<Any>().setOutgoingSignalBuffered(signal, signalHandler)
			}
		} else {
			when (portType) {
				PortType.INOUT -> if (fromOutside) {
					getOutput<Any>().setOutgoingSignalBuffered(signal, signalHandler)
				} else {
					propagateToSubGraphOutputPort(signal, signalHandler)
				}
				PortType.INPUT -> getOutput<Any>().setOutgoingSignalBuffered(signal, signalHandler)
				PortType.OUTPUT -> propagateToSubGraphOutputPort(signal, signalHandler)
			}
		}
	}

	private fun propagateToSubGraphOutputPort(signal: DigitalSignal, signalHandler: SignalHandler) {
		subGraphOutputPort?.propagateSignal(signal, signalHandler)
	}
}