package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.port.PortImpl

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
	protected val calculator: VerticeCalculator<*> = EmptyVerticeCalculator,
	name: String? = null
) : AbstractVertice(name) {

	override fun act(signalHandler: SignalHandler, data: ActorData) {
		actImpl(signalHandler, data)
		super.act(signalHandler, data)
	}

	protected open fun actImpl(signalHandler: SignalHandler, data: ActorData) {
		(calculator as VerticeCalculator<CalculatingVertice>).calculate(this, data as GraphActorData, signalHandler)
		flush(signalHandler, data)
		stateChanged(signalHandler)
	}

	protected open fun flush(signalHandler: SignalHandler, data: ActorData) {
		getOutputs().forEach { port ->
			if (port !== (data as GraphActorData).changedPort) {
				// Don't flush OutputPorts that triggered execution to avoid shooting back signals
				port.flush(signalHandler)

				/*
				if (port.portType.isInput && port is PortImpl) {
					port.syncIncomingSignalWithNegotiatedOutgoingSignal()
				}
				*/
			}
		}
	}
}
