package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.port.PortImpl

/**
 * A standard implementation of the [GraphInput] interface whose [PortType] cannot be changed.
 * @param clickValue the value to appear at the output if the user clicks on this [GraphInputImpl]
 */
class GraphInputImpl<T: Any>(
	outputPort: OutputPort<T> = PortImpl(PortType.OUTPUT),
	name: String? = null,
	eventBus: EventBus = BaseModule.eventBus,
	private val clickValue: T? = null
) : AbstractGraphPort<T>(port = outputPort, name = name, eventBus = eventBus, calculator = CALCULATOR), GraphInput<T> {

	companion object {
		private const val BASE_RESOURCE_KEY = "graph.element.input"
		private val type = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val typeDesc = Translations.getString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<GraphInputImpl<Any>> {
			override fun calculate(vertice: GraphInputImpl<Any>, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.getOutput<Any>().setOutgoingSignalBuffered((data as StoringGraphActorData).signal, signalHandler)
			}
		}
	}

	override val type: String get() = GraphInputImpl.type
	override val typeDesc: String get() = GraphInputImpl.typeDesc

	fun handleClick(signalHandler: SignalHandler) {
		if (clickValue != null) {
			setIncomingSignal(clickValue, signalHandler)
		}
	}

    /** ---- [GraphPort] interface */

    @Suppress("UNUSED_PARAMETER")
    override var portType: PortType
        get() = PortType.INPUT
        set(value) {
            throw UnsupportedOperationException("cannot set PortType")
        }

    /** ---- [GraphInput] interface */

    override var subGraphInputPort: SubGraphInputPort<T>? = null

    override fun setIncomingSignal(signal: T?, signalHandler: SignalHandler, force: Boolean) {
	    this.signal = signal
    	requestActingAfter(signalHandler, propagationDelay, StoringGraphActorData(null, signal, force = force))
    }
}