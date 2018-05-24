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
interface VerticeCalculator<in T: Vertice> {

    /** Calculates and sets the [OutputPort]s of a [Vertice]. */
    fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler)
}

/** An empty implementation of the [VerticeCalculator] interface that does nothing.*/
object EmptyVerticeCalculator : VerticeCalculator<CalculatingVertice> {

    override fun calculate(vertice: CalculatingVertice, data: GraphActorData, signalHandler: SignalHandler) {
        // empty
    }
}

open class CalculatingVertice(
    private val calculator: VerticeCalculator<*>,
    name: String? = null
) : AbstractVertice(name) {

    constructor(name: String? = null): this(EmptyVerticeCalculator, name)

    override fun act(signalHandler: SignalHandler, data: ActorData): Boolean {
        (calculator as VerticeCalculator<CalculatingVertice>).calculate(this, data as GraphActorData, signalHandler)
        getOutputs().forEach { it.flush(signalHandler) }
        stateChanged(signalHandler)
        return super.act(signalHandler, data)
    }
}
