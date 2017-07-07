package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.graph.model.GraphInput
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.SubGraphInputPort

/**
 * A standard implementation of the [GraphInput] interface whose [PortType] cannot be changed.
 */
class GraphInputImpl<T: Any>(outputPort: OutputPort<T>) : CalculatingVertice(), GraphInput<T> {

    init {
        propagationDelay = 0
        addPort(outputPort)
    }

    /** ---- [GraphPort] interface */

    override val signal: T? get() = getOutput<T>().getOutgoingSignal()

    override var portType: PortType
        get() = PortType.INPUT
        set(value) {
            throw UnsupportedOperationException("cannot set PortType")
        }

    /** ---- [GraphInput] interface */

    override var subGraphInputPort: SubGraphInputPort<T>? = null

    override fun setIncomingSignal(signal: T?, signalHandler: SignalHandler) {
        getOutput<T>().setOutgoingSignalBuffered(signal, signalHandler)
    }
}