package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.base.checkState
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.element.AbstractGraphElement
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.logger
import kotlin.NoSuchElementException

/**
 * Standard implementation of the [Net] interface.
 */
open class NetImpl<T: Any> : AbstractGraphElement(), Net<T> {

    companion object {
        private val LOG by logger(NetImpl::class)
    }

    /** Internal representation of the [ports] property.*/
    private val _ports = mutableListOf<Port<T>>()

    /** ---- [Net] interface */

    /** Non-property variable in order to access field while allowing to override getter of [signal] in subclasses.*/
    private var _signal: T? = null

    override val signal: T?
        get() = _signal

    override var signalBuffer: T? = null

    override val inconsistent: Boolean
        get() {
            var definedSignal: T? = null
            getOutputPorts()
                    .asSequence()
                    .filterNot { it.isOutputUndefined }
                    .forEach {
                        if (definedSignal == null) {
                            definedSignal = it.getOutgoingSignal()
                        } else {
                            if (!SignalUtil.equals(definedSignal, it.getOutgoingSignal())) {
                                return true
                            }
                        }
                    }
            return false
        }

    override val portsCount: Int
        get() = _ports.size

    override val ports: ImmutableList<Port<T>>
        get() = ImmutableList(_ports)

    override fun connect(port: Port<T>) {
        checkState(!_ports.contains(port), "Net already connected to specified Port")
        LOG.trace("NetImpl: connect ${port.portId}")
        _ports.add(port)
        port.connectTo(this)
        stateChanged()
    }

    override fun unconnect(port: Port<*>) {
        checkState(_ports.contains(port), "Net not connected with specified Port")
        LOG.trace("NetImpl: unconnect ${port.portId}")
        _ports.remove(port)
        port.disconnect()
        stateChanged()
    }

    override fun isConnectedWith(port: Port<T>): Boolean {
        return _ports.contains(port)
    }

    override fun getConsistentSignalPort(): OutputPort<T>? {
        var consistentPort: OutputPort<T>? = null
        for (port in getOutputPorts()) {
            if (!port.isOutputUndefined) {
                if (consistentPort == null) {
                    consistentPort = port
                } else {
                    if (!SignalUtil.equals(consistentPort.getOutgoingSignal(), port.getOutgoingSignal())) {
                        return null
                    }
                }
            }
        }
        return consistentPort
    }

    override fun setSignal(signal: T?, origin: OutputPort<T>, signalHandler: SignalHandler, withDelay: Boolean) {
        if (!SignalUtil.equals(_signal, signal)) {
            signalBuffer = signal
            val data = NetActorData(signal, origin)
            if (withDelay) {
                requestActingTimeFreeze(signalHandler, data)
            } else {
                actingDone(signalHandler, data)
            }
        }
    }

    /** ---- [Actor] interface */

    override fun act(signalHandler: SignalHandler, data: ActorData): Boolean {
        return notifyActed(signalHandler, data)
    }

    override fun executionStarted(signalHandler: SignalHandler) {
        super.executionStarted(signalHandler)
        _signal = null
        signalBuffer = null
    }

    override fun actingDone(signalHandler: SignalHandler, data: ActorData) {
        _signal = (data as GraphActorData).getSignal(1)
        stateChanged()
        ports
            .filter { it.portType.isInput && it != data.changedPort }
            .map { it as InputPort }
            .forEach { it.setIncomingSignal(signal, signalHandler) }
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        val portRefs = _ports.map { PortRef(it) }
        if (!portRefs.isEmpty()) {
            writer.writeStorables("ports", portRefs.iterator())
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        for (portRef in reader.readStorables("ports").map{it as PortRef<*>}) {
            reader.requestResolution(this, Reference(
                name = "portRef",
                referenceId = portRef.verticeId,
                additionalInfo = portRef
            ))
        }
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        super.resolve(reference, referenceResolver)
        if (reference.name == "portRef") {
            val vertice = referenceResolver.getStorable(reference.referenceId) as Vertice
            val portId = (reference.additionalInfo as PortRef<T>).portId
            try {
                val port = vertice.getPort<T>(portId)
                _ports.add(port)
                port.connectTo(this)
            } catch (e: NoSuchElementException) {
                LOG.error("NetImpl: Couldn't resolve Port $portId of Vertice ${System.get().getClassName(vertice)} with storableID ${vertice.storableId}")
            }
        }
    }

    /** ---- [NetImpl] */

    private fun getOutputPorts(): Collection<OutputPort<T>> {
        return _ports
            .filter { it.portType.isOutput }
            .map { it as OutputPort<T> }
    }

    /** Used for storing references to the [Port]s of a [Net].*/
    class PortRef<T: Any>(val port: Port<T>? = null) : Storable {

        override var storableId: Int = -1

        var verticeId: Int = -1
        var portId: Int = -1

        override fun write(writer: StoreWriter) {
            writer.writeInt("verticeId", writer.provideIdentity(port!!.owner!!))
            writer.writeInt("portId", port.portId)
        }

        override fun read(reader: StoreReader) {
            verticeId = reader.readInt("verticeId")
            portId = reader.readInt("portId")
        }

        override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
            // empty
        }

        override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()
    }
}