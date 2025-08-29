package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.graph.model.*

/**
 * Calculates the maximum of the sum of all propagation delays when traversing a [Graph]
 * from any [InputPort] to any [OutputPort].
 */
class GraphPropagationDelayCalculator {

    companion object {
        /** The name of the [Boolean] property in [Properties] calculation upon save.*/
        const val PROP_CALCULATE_ON_SAVE = "graph.model.graph.calculatePropDelayOnSave"
    }

    /**
     * @return -1 if no [InputPort]s exist in the [Graph].
     */
    fun calculate(graph: Graph): Long {
        val propDelay = graph.graphPorts
            .filter { it.portType.isOutput }
            .filter { it.getPort<Any>().net != null }
            .maxOfOrNull {
                calculateBackwardsFromVertice(it, Stack())
            } ?: -1L
        return propDelay
    }

    private fun calculateBackwardsFromInputPort(vertice: Vertice, inputPort: Port<*>, path: Stack<Vertice>): Long {
        val propDelay = vertice.propagationDelay.value
        if (inputPort.net != null && !path.contains(vertice)) {
            path.push(vertice)
            val maxValue = inputPort.net?.ports
                ?.filter { it.portType.isOutput }
                ?.filter { it.owner !is GraphPort<*> }
                ?.maxOfOrNull {
                    calculateBackwardsFromVertice(it.owner!!, path)
                }
                ?: 0L
            path.pop()
            return propDelay + maxValue
        } else {
            return 0
        }
    }

    private fun calculateBackwardsFromVertice(vertice: Vertice, path: Stack<Vertice>): Long {
        val result = vertice.getInputs()
            .maxOfOrNull { calculateBackwardsFromInputPort(vertice, it, path) }
            ?: 0L

        return result
    }
}