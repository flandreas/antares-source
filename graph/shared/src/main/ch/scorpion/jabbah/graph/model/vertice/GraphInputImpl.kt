package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.port.PortImpl

/**
 * A standard implementation of the [GraphInput] interface whose [PortType] cannot be changed.
 */
class GraphInputImpl<T: Any>(
	outputPort: OutputPort<T> = PortImpl(PortType.OUTPUT),
	name: String? = null,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphPort<T>(port = outputPort, name = name, eventBus = eventBus, calculator = CALCULATOR), GraphInput<T> {

	companion object {
		private const val baseResourceKey = "graph.element.input"
		private val type = Translations.getString("$baseResourceKey.name")
		private val typeDesc = Translations.getString("$baseResourceKey.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<GraphInputImpl<Any>> {
			override fun calculate(vertice: GraphInputImpl<Any>, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.getOutput<Any>().setOutgoingSignalBuffered((data as StoringGraphActorData).signal, signalHandler)
			}
		}
	}

	override val type: String get() = GraphInputImpl.type
	override val typeDesc: String get() = GraphInputImpl.typeDesc

    /** ---- [GraphPort] interface */

    override var signal: T? = null

    @Suppress("UNUSED_PARAMETER")
    override var portType: PortType
        get() = PortType.INPUT
        set(value) {
            throw UnsupportedOperationException("cannot set PortType")
        }

    /** ---- [GraphInput] interface */

    override var subGraphInputPort: SubGraphInputPort<T>? = null

    override fun setIncomingSignal(signal: T?, signalHandler: SignalHandler) {
	    this.signal = signal
    	requestActingAfter(signalHandler, propagationDelay, StoringGraphActorData(null, signal))
    }
}