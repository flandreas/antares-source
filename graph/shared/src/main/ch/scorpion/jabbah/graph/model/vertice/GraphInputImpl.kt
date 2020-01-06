package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.GraphInput
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.SubGraphInputPort
import ch.scorpion.jabbah.graph.model.port.PortImpl

/**
 * A standard implementation of the [GraphInput] interface whose [PortType] cannot be changed.
 */
class GraphInputImpl<T: Any>(
	outputPort: OutputPort<T> = PortImpl(PortType.OUTPUT),
	name: String? = null,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphPort<T>("graph.element.input", port = outputPort, name = name, eventBus = eventBus), GraphInput<T> {

    /** ---- [GraphPort] interface */

    override val signal: T? get() = getOutput<T>().getOutgoingSignal()

    @Suppress("UNUSED_PARAMETER")
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