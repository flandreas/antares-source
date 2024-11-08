package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.graph.model.*

/**
 * Calculates the maximum of the sum of all propagation delays when traversing a [Graph]
 * from any [InputPort] to any [OutputPort].
 */
class GraphPropagationDelayCalculator {

    private val path = Stack<OutputPort<*>>()

    fun calculate(graph: Graph): Long =
        graph.graphInputs.maxOf { calculateFrom(it.getOutput<Any>()) }

    private fun calculateFrom(outputPort: OutputPort<*>): Long {
        path.push(outputPort)
        val value = outputPort.net?.ports
            ?.filter { it.portType.isInput }
            ?.filter { it.owner !is GraphPort<*> }
            ?.maxOfOrNull { calculateFrom(it.owner!!) }
            ?: 0L
        path.pop()
        return value
    }

    private fun calculateFrom(vertice: Vertice): Long {
        return vertice.getOutputs()
            .filter { !path.contains(it) }
            .maxOfOrNull { vertice.propagationDelay.value + calculateFrom(it) }
            ?: 0L
    }
}