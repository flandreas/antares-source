package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.port.PortImpl

/**
 * A standard implementation of the [GraphOutput] interface whose [PortType] cannot be changed.
 */
class GraphOutputImpl<T: Any>(
	inputPort: InputPort<T> = PortImpl(PortType.OUTPUT),
	name: String? = null,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphPort<T>(port = inputPort, name = name, calculator = GraphOutputImplCalculator, eventBus = eventBus), GraphOutput<T> {

    private var subGraphOutputPort: SubGraphOutputPort<T>? = null

    /** ---- [GraphPort] interface */

    override var signal: T? = null

    override var portType: PortType
        get() = PortType.OUTPUT
        set(value) {
            throw UnsupportedOperationException("cannot set PortType")
        }

    /** ---- [GraphOutput] interface */

    override fun setSubGraphOutputPort(port: SubGraphOutputPort<T>) {
        subGraphOutputPort = port
    }

    /** ---- [CalculatingVertice] */

    override fun act(signalHandler: SignalHandler, data: ActorData): Boolean {
        super.act(signalHandler, data)
        subGraphOutputPort?.flush(signalHandler)
        return false
    }

    /** ---- [GraphOutputImpl] */

    fun setOutgoingSignal(signal: T?, signalHandler: SignalHandler) {
        this.signal = signal
        stateChanged()
        subGraphOutputPort?.setOutgoingSignalBuffered(signal, signalHandler)
    }
}

object GraphOutputImplCalculator : VerticeCalculator<GraphOutputImpl<*>> {

    override fun calculate(vertice: GraphOutputImpl<*>, data: GraphActorData, signalHandler: SignalHandler) {
        vertice.setOutgoingSignal(data.getSignal(1), signalHandler)
    }
}