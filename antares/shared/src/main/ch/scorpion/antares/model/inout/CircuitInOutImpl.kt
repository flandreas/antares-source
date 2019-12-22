package ch.scorpion.antares.model.inout

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.model.signal.DigitalSignalUtil
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.SignalUtil.differ
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
	portType: PortType = PortType.INPUT
) : AbstractGraphPort<DigitalSignal>(
	baseResourceKey = "library.element.CircuitInOut",
	port = DigitalPortImpl(portType.reverse()),
	name = name,
	calculator = CALCULATOR
), CircuitInOut {

	companion object {
		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<CircuitInOutImpl> {
			override fun calculate(vertice: CircuitInOutImpl, data: GraphActorData, signalHandler: SignalHandler) {
				with(vertice) {
					setOutgoingSignal(data.getSignal(1)!!, signalHandler, data.changedPort == null)
					enabled = true
				}
			}
		}
	}

	private var readingFromStore = false

	/** ---- [GraphPort] interface */

	/** Captures the [DigitalSignal] that has been process last, either as input or as output. */
	override var signal: DigitalSignal? = null
		get() = field ?: DigitalSignalUtil.undefined(bitWidth)

	override var portType: PortType
		get() = getDigitalPort().portType.reverse()
		set(value) {
			if (portType != value) {
				getDigitalPort().portType = value.reverse()
			}
		}

	/** ---- [GraphInput] interface */

	override var subGraphInputPort: SubGraphInputPort<DigitalSignal>? = null

	override fun setIncomingSignal(signal: DigitalSignal?, signalHandler: SignalHandler) {
		if (enabled && differ(this.signal, signal)) {
			this.signal = signal
			enabled = false
			requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, this.signal))
		}
	}

	/** ---- [GraphOutput] */

	private var _subGraphOutputPort: SubGraphOutputPort<DigitalSignal>? = null

	override fun setSubGraphOutputPort(port: SubGraphOutputPort<DigitalSignal>) {
		_subGraphOutputPort = port
	}

	/** ---- [Actor] interface */

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		signal = getDigitalPort().defaultDigitalSignal
		if (portType.isInput) {
			requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(getOutput<DigitalSignal>(), signal))
		}
		stateChanged(signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		resetExecutionState(signalHandler)
	}

	override fun act(signalHandler: SignalHandler, data: ActorData) {
		super.act(signalHandler, data)
		if (portType.isOutput && _subGraphOutputPort != null) {
			_subGraphOutputPort?.flush(signalHandler)
		}
	}

	private fun resetExecutionState(signalHandler: SignalHandler) {
		signal = getDigitalPort().defaultDigitalSignal
		if (portType.isInput) {
			getOutput<DigitalSignal>().setOutgoingSignal(signal, signalHandler)
		}
		stateChanged()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("type", portType.customName)
		writer.writeInt("bitWidth", bitWidth.width)
		portDescription.write("desc", writer)
	}

	override fun read(reader: StoreReader) {
		try {
			readingFromStore = true
			super.read(reader)
			portType = PortType.withName(reader.readString("type"))
			bitWidth = BitWidth.of(reader.readInt("bitWidth"))
			portDescription.read("desc", reader)
		} finally {
			readingFromStore = false
		}
	}

	/** ---- [CircuitInOut] interface */

	override var signalRepresentation: DigitalSignalRepresentation
		get() = getDigitalPort().signalRepresentation
		set(value) {
			if (value != getDigitalPort().signalRepresentation) {
				val oldValue = getDigitalPort().signalRepresentation
				getDigitalPort().signalRepresentation = value
				if (!readingFromStore) {
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
				if (!readingFromStore) {
					eventBus.post(CircuitInOutBitWidthChanged(this, oldValue, value))
				}
			}
		}

	/** ---- [CircuitInOutImpl] */

	private fun getDigitalPort(): DigitalPort = getPort<DigitalPort>() as DigitalPort

	private fun setOutgoingSignal(signal: DigitalSignal, signalHandler: SignalHandler, fromOutside: Boolean) {
		this.signal = signal
		stateChanged()

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

	private fun propagateToSubGraphOutputPort(signal: DigitalSignal, signalHandler: SignalHandler) {
		_subGraphOutputPort?.propagateSignal(signal, signalHandler)
	}
}