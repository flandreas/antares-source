package ch.scorpion.antares.model.inout

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.model.signal.DigitalSignalUtil
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter



/**
 * A standard implementation of the {@link CircuitInOut} interface.
 */
class CircuitInOutImpl(
    val eventBus: EventBus,
    portType: PortType
) : CalculatingVertice(CALCULATOR), CircuitInOut {

    constructor(eventBus: EventBus): this(eventBus, PortType.INPUT)
    constructor(): this(BaseModule.eventBus)

    companion object {

        val CALCULATOR = object : VerticeCalculator<CircuitInOutImpl> {
            override fun calculate(vertice: CircuitInOutImpl, data: GraphActorData, signalHandler: SignalHandler) {
                vertice.setOutgoingSignal(data.getSignal<DigitalSignal>(1)!!, signalHandler, data.changedPort == null)
            }
        }
    }

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

    init {
        addPort(DigitalPortImpl(portType.reverse()))
    }

    /** ---- [GraphInput] interface */

    override var subGraphInputPort: SubGraphInputPort<DigitalSignal>? = null

    override fun setIncomingSignal(signal: DigitalSignal?, signalHandler: SignalHandler) {
        this.signal = signal
        stateChanged()
        actorSupport.requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, this.signal))
    }

    /** ---- [GraphOutput] */

    private var _subGraphOutputPort: SubGraphOutputPort<DigitalSignal>? = null

    override fun setSubGraphOutputPort(port: SubGraphOutputPort<DigitalSignal>) {
        _subGraphOutputPort = port
    }

    /** ---- [Actor] interface */

    override fun executionStarted(signalHandler: SignalHandler) {
        super.executionStarted(signalHandler)
        resetExecutionState(signalHandler)
    }

    override fun executionStopped(signalHandler: SignalHandler) {
        super.executionStopped(signalHandler)
        resetExecutionState(signalHandler)
    }

    override fun act(signalHandler: SignalHandler, data: ActorData): Boolean {
        super.act(signalHandler, data)
        if (portType.isOutput && _subGraphOutputPort != null) {
            _subGraphOutputPort?.flush(signalHandler)
        }
        return false
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
        if (!StringUtils.isBlank(portDescription)) {
            writer.writeString("desc", portDescription!!)
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        portType = PortType.withName(reader.readString("type"))
        bitWidth = BitWidth.of(reader.readInt("bitWidth"))
        if (reader.hasAttribute(("desc"))) {
            shortDescription = reader.readString("desc")
        }
    }

    /** ---- [Vertice] interface */

    override var name: String?
        get() = super.name
        set(value) {
            if (super.name != value) {
                val oldName = super.name
                super.name = value
                stateChanged()
                eventBus.postVetoable(
                    event = GraphPortNameChanged(this, oldName, value),
                    undoEvent = GraphPortNameChanged(this, value, oldName),
                    vetoHandler = {
                        super.name = oldName
                        stateChanged()
                        // TODO Post an application error event that can be displayed to the user as an info
                    }
                )
            }
        }

    /** ---- [CircuitInOut] interface */

    override var signalRepresentation: DigitalSignalRepresentation
        get() = getDigitalPort().signalRepresentation
        set(value) {
            if (value != getDigitalPort().signalRepresentation) {
                val oldValue = getDigitalPort().signalRepresentation
                getDigitalPort().signalRepresentation = value
                eventBus.post(CircuitInOutSignalRepresentationChanged(this, oldValue, value))
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
                eventBus.post(CircuitInOutBitWidthChanged(this, oldValue, value))
            }
        }

    /** ---- [CircuitInOutImpl] */

    private fun getDigitalPort(): DigitalPort {
        return getPort<DigitalPort>() as DigitalPort
    }

    private fun setOutgoingSignal(signal: DigitalSignal, signalHandler: SignalHandler, fromOutside: Boolean) {
        this.signal = signal
        stateChanged()

        when (portType) {
            PortType.INOUT -> if (fromOutside) {
                getOutput<Any>().setOutgoingSignalBuffered(signal, signalHandler)
            } else {
                _subGraphOutputPort?.setOutgoingSignalBuffered(signal, signalHandler)
            }
            PortType.INPUT -> getOutput<Any>().setOutgoingSignalBuffered(signal, signalHandler)
            PortType.OUTPUT -> _subGraphOutputPort?.setOutgoingSignalBuffered(signal, signalHandler)
        }
    }
}