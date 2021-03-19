package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort

/**
 * Can be plugged into a [CalculatingVertice] to calculate new signals on the [Vertice]' [OutputPort]
 * whenever a signal has arrived on one of the [Vertice]' [InputPort]s.
 * @param <T> the type of [CalculatingVertice] that this [VerticeCalculator] calculates.
 */
interface VerticeCalculator<in T : Vertice> {

	/** Calculates and sets the [OutputPort]s of a [Vertice]. */
	fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler)
}

/** An empty implementation of the [VerticeCalculator] interface that does nothing.*/
object EmptyVerticeCalculator : VerticeCalculator<CalculatingVertice> {

	override fun calculate(vertice: CalculatingVertice, data: GraphActorData, signalHandler: SignalHandler) {
		// empty
	}
}

abstract class CalculatingVertice(
	private val calculator: VerticeCalculator<*> = EmptyVerticeCalculator,
	name: String? = null
) : AbstractVertice(name) {

	override fun act(signalHandler: SignalHandler, data: ActorData) {
		actImpl(signalHandler, data)
		super.act(signalHandler, data)
	}

	protected open fun actImpl(signalHandler: SignalHandler, data: ActorData) {
		(calculator as VerticeCalculator<CalculatingVertice>).calculate(this, data as GraphActorData, signalHandler)
		getOutputs().forEach { it.flush(signalHandler) }
		stateChanged(signalHandler)
	}
}
