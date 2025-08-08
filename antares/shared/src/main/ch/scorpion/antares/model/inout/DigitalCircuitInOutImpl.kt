package ch.scorpion.antares.model.inout

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.vertice.AdjustableBitWidth
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.SignalUtil.differ
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.vertice.InteractableVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A standard implementation of the [DigitalCircuitInOut] interface.
 */
class DigitalCircuitInOutImpl(
	eventBus: EventBus = BaseModule.eventBus,
	name: String? = null,
	portType: PortType = PortType.INPUT,
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractCircuitInOut<DigitalSignal>(
	port = DigitalPortImpl(portType.reverse(), bitWidth = bitWidth, canBeUndefined = portType == PortType.INOUT),
	name = name,
	calculator = CALCULATOR,
	eventBus
), DigitalCircuitInOut {

	companion object {

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<DigitalCircuitInOutImpl> {
			override fun calculate(vertice: DigitalCircuitInOutImpl, data: GraphActorData, signalHandler: SignalHandler) {
				with(vertice) {
					calculate(signalHandler)
					setOutgoingSignal(data.getSignal(1)!!, signalHandler, data.changedPort == null)
					setInteractionEnabled(true, signalHandler)
				}
			}
		}
	}

	/** ---- [WeakOutputPortBehaviour] */

	override val isWeekOutputPortBehaviour: Boolean get() = portType.isOutput && subGraphOutputPort == null

	override fun withdrawWeakOutput(
		netSignal: DigitalSignal?,
		port: OutputPort<DigitalSignal>,
		signalHandler: SignalHandler
	) {
		port.setOutgoingSignalBuffered(DigitalSignalFactory.undefined((port as DigitalPort).bitWidth), signalHandler)
		stateChanged(signalHandler)
	}

	override fun activateWeakOutput(
		netSignal: DigitalSignal?,
		port: OutputPort<DigitalSignal>,
		signalHandler: SignalHandler
	): DigitalSignal {
		withdrawWeakOutput(netSignal, port, signalHandler)
		return DigitalSignalFactory.undefined((port as DigitalPort).bitWidth)
	}

	override fun handleNetChanged(signalHandler: SignalHandler) {
		signal = getPort<DigitalSignal>().net!!.signal
		stateChanged(signalHandler)
	}

	/** ---- [DigitalSignalSource] */

	override var fixedPointConfig: FixedPointConfig? = null

	/** ---- [AdjustableBitWidth] */

	override fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean {
		this.bitWidth = bitWidth
		stateChanged()
		return true
	}

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	/** ---- [GraphPort] interface */

	/** Captures the [DigitalSignal] that has been process last, either as input or as output. */
	override var signal: DigitalSignal? = null
		get() = field ?: DigitalSignalFactory.undefined(bitWidth)

	/** ---- [GraphInput] interface */

	override var startValue: DigitalSignal? = null
		set(value) {
			if (value != startValue) {
				val oldValue = startValue
				field = value
				if (isNotReading) {
					stateChanged(null)
					eventBus.post(DigitalCircuitInOutStartValueChanged(this, oldValue, value))
				}
			}
		}

	override fun setIncomingSignal(signal: DigitalSignal?, signalHandler: SignalHandler, force: Boolean) {
		setIncomingSignal(signal, signalHandler, propagationDelay.value, force)
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

	/** ---- [NetCombiner] interface */

	/** Only required for toplevel [DigitalCircuitInOutImpl] that can produce a signal when the user clicks on it. */
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

	/** ---- [InteractableVertice] interface */

	override var interactivePropagationDelay: Long = Switch.DEF_PROP_DELAY.value

	/** ---- [Vertice] */

	override fun <T : Any> replaceUndefinedOutput(signal: T?) {
		if (signal is DigitalSignal?) {
			signal?.let { this.signal = it }
		}
	}

	override fun <T : Any> notifyResendSignal(port: OutputPort<T>, signalHandler: SignalHandler) {
		signal = getDigitalPort().getOutgoingSignal()
		stateChanged(signalHandler)
	}

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		setInteractionEnabled(true, signalHandler)
		signal = if (startValue != null) {
			startValue
		} else {
			getDigitalPort().dominantSignal
		}
		stateChanged(signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay.value, StoringGraphActorData(null, signal))
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		resetExecutionState(signalHandler)
	}

	override fun act(signalHandler: SignalHandler, data: ActorData) {
		super.act(signalHandler, data)

		if (portType.isOutput && subGraphOutputPort != null) {
			if ((data as GraphActorData).changedPort != null || signalHandler.executionTime == propagationDelay.value) {
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

	override fun createActorData(inputPort: InputPort<*>?, force: Boolean, signal: Any?): GraphActorData =
		if (inputPort == null) {
			StoringGraphActorData(null, signal ?: this.signal)
		} else {
			super.createActorData(inputPort, force, signal)
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		bitWidth.write("bitWidth", writer)
		if (startValue != null) {
			writer.writeULong("startValue", startValue!!.getValue())
		}
		if (interactivePropagationDelay != Switch.DEF_PROP_DELAY.value) {
			writer.writeLong("interactivePropagationDelay", interactivePropagationDelay)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.read("bitWidth", reader)
		if (reader.hasAttribute("startValue")) {
			startValue = DigitalSignalFactory.of(bitWidth, reader.readULong("startValue"))
		}
		if (reader.hasAttribute("interactivePropagationDelay")) {
			interactivePropagationDelay = reader.readLong("interactivePropagationDelay")
		}
	}

	/** ---- [CircuitInOut] interface */

	override fun setSignalManually(signal: DigitalSignal, signalHandler: SignalHandler) {
		requestSetSignal(signal, signalHandler)
	}

	/** ---- [DigitalCircuitInOut] */

	override var signalRepresentation: DigitalSignalRepresentation
		get() = getDigitalPort().signalRepresentation
		set(value) {
			if (value != getDigitalPort().signalRepresentation) {
				val oldValue = getDigitalPort().signalRepresentation
				getDigitalPort().signalRepresentation = value
				if (isNotReading) {
					eventBus.post(DigitalCircuitInOutSignalRepresentationChanged(this, oldValue, value))
				}
			}
		}

	override var bitWidth: BitWidth
		get() = getDigitalPort().bitWidth
		set(value) {
			if (value != getDigitalPort().bitWidth) {
				signal = null
				val oldValue = getDigitalPort().bitWidth
				getDigitalPort().bitWidth = value
				if (isNotReading) {
					stateChanged(null)
					eventBus.post(DigitalCircuitInOutBitWidthChanged(this, oldValue, value))
				}
			}
		}

	override fun toggleBit(index: Int, undefine: Boolean, signalHandler: SignalHandler, graphView: GraphView?) {
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
		setSignalManually(s.withBit(index, bit.not()), signalHandler)
	}

	/** ---- [DigitalCircuitInOutImpl] */

	private fun getDigitalPort(): DigitalPort = getPort<DigitalPort>() as DigitalPort

	private fun setOutgoingSignal(signal: DigitalSignal, signalHandler: SignalHandler, fromOutside: Boolean) {
		this.signal = signal
		stateChanged(signalHandler)

		if (signalHandler.executionTime == propagationDelay.value) {
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