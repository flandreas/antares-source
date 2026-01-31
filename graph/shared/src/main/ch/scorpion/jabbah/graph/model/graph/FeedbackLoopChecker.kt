package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef

/**
 * Checks recursively if a given [Graph] has any [Vertice] with an [OutputPort]
 * that is connected directly or indirectly to one if its [InputPort InputPorts]
 * by a [Net].
 */
object FeedbackLoopChecker {

    fun hasFeedbackLoop(graph: Graph): Boolean {
        return graph.purelyScripted ||
            hasLocalFeedbackLoop(graph) ||
            graph.elements
                .filterIsInstance<SubGraphVerticeRef>()
                .any { it.getGraphIfNotBroken()?.let { subGraph -> hasFeedbackLoop(subGraph) } == true }
    }

    private fun hasLocalFeedbackLoop(graph: Graph): Boolean {
        return graph.graphPorts
            .filter { it.portType.isInput && it.getPort<Any>().net != null}
            .any { hasLocalFeedbackLoop(it, Stack()) }
    }

    private fun hasLocalFeedbackLoop(vertice: Vertice, path: Stack<Vertice>): Boolean =
        vertice.getOutputs().any { hasLocalFeedbackLoop(vertice, it, path) }

    private fun hasLocalFeedbackLoop(vertice: Vertice, outputPort: Port<*>, path: Stack<Vertice>): Boolean {
        if (path.contains(vertice)) {
            return true
        }
        return if (outputPort.net != null) {
            path.push(vertice)
            val b = outputPort.net!!.ports
                .filter { it !== outputPort && it.portType.isInput && it.owner !is GraphPort<*>}
                .any { hasLocalFeedbackLoop(it.owner!!, path) }
            path.pop()
            b
        } else {
            false
        }
    }
}