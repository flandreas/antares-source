package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.net.*
import ch.scorpion.jabbah.graph.model.port.PortImpl

/**
 * A standard implementation of the [GraphOutput] interface whose [PortType] cannot be changed.
 */
class GraphOutputImpl<T : Any>(
	inputPort: InputPort<T> = PortImpl(PortType.INPUT),
	name: String? = null,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphPort<T>(port = inputPort, name = name, calculator = GraphOutputImplCalculator, eventBus = eventBus), GraphOutput<T> {

	companion object {
		private const val baseResourceKey = "graph.element.output"
		private val type = Translations.getString("$baseResourceKey.name")
		private val typeDesc = Translations.getOptionalString("$baseResourceKey.desc")

	}

	override val type: String get() = GraphOutputImpl.type
	override val typeDesc: String? get() = GraphOutputImpl.typeDesc

	/** ---- [GraphPort] interface */

	override var signal: T? = null

	@Suppress("UNUSED_PARAMETER")
	override var portType: PortType
		get() = PortType.OUTPUT
		set(value) {
			throw UnsupportedOperationException("cannot set PortType")
		}

	/** ---- [GraphOutput] interface */

	override var subGraphOutputPort: SubGraphOutputPort<T>? = null

	/** ---- [CalculatingVertice] */

	override fun act(signalHandler: SignalHandler, data: ActorData) {
		super.act(signalHandler, data)
		subGraphOutputPort?.flush(signalHandler)
	}

	/** ---- [NetCombiner] */

	override fun <T : Any> createCombinedNetsFor(outputPort: OutputPort<T>, inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<CombinedNet<T>> {
		val result = if (subGraphOutputPort == null) {
			emptyList()
		} else {
			CombinedNet.createFor(subGraphOutputPort!!, signalHandler) as Collection<CombinedNet<T>>
		}

		result.forEach { it.replaceAccessPort(subGraphOutputPort as OutputPort<T>, outputPort) }

		return result
	}

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean = false

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