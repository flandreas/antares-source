package io.antarescircuit.jabbah.graph.model.graph

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.graph.model.*

/**
 * Calculates the maximum of the sum of all propagation delays when traversing a [Graph]
 * from any [InputPort] to any [OutputPort].
 *
 * Feedback loops are cut where the traversal reaches a [Vertice] already on the current path. The delay of every
 * [Vertice] is calculated once, so the calculation takes linear time instead of enumerating all paths, which
 * grows exponentially with the number of fan-outs.
 */
class GraphPropagationDelayCalculator {

    companion object {
        /** The name of the [Boolean] property in [Properties] calculation upon save.*/
        const val PROP_CALCULATE_ON_SAVE = "graph.model.graph.calculatePropDelayOnSave"
    }

    /** The longest delay from a graph input up to (and including) a [Vertice], for all completely calculated ones. */
    private val delays = mutableMapOf<Vertice, Long>()

    private val path = mutableSetOf<Vertice>()

    /**
     * @return -1 if no [InputPort]s exist in the [Graph].
     */
    fun calculate(graph: Graph): Long {
        delays.clear()
        val propDelay = graph.graphPorts
            .filter { it.portType.isOutput }
            .filter { it.getPort<Any>().net != null }
            .maxOfOrNull { calculateBackwardsFromVertice(it) }
            ?: -1L
        return propDelay
    }

    private fun calculateBackwardsFromInputPort(vertice: Vertice, inputPort: Port<*>): Long {
        val net = inputPort.net
        if (net == null || vertice in path) return 0
        path.add(vertice)
        val maxValue = net.ports
            .filter { it.portType.isOutput }
            .filter { it.owner !is GraphPort<*> }
            .maxOfOrNull { calculateBackwardsFromVertice(it.owner!!) }
            ?: 0L
        path.remove(vertice)
        return vertice.propagationDelay.value + maxValue
    }

    private fun calculateBackwardsFromVertice(vertice: Vertice): Long {
        delays[vertice]?.let { return it }
        val result = vertice.getInputs()
            .maxOfOrNull { calculateBackwardsFromInputPort(vertice, it) }
            ?: 0L
        // Results of vertices on the current path are incomplete, because the loop through them has been cut
        if (vertice !in path) delays[vertice] = result
        return result
    }
}
