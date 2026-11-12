package io.antarescircuit.jabbah.graph.model.graph

import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef

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
        val search = CycleSearch()
        return graph.graphPorts
            .filter { it.portType.isInput && it.getPort<Any>().net != null }
            .any { search.reachesCycle(it) }
    }

    /**
     * Depth-first search that visits each [Vertice] once (O(V+E)). Enumerating all paths instead is
     * exponential when signal paths fan out and rejoin, which froze the UI for larger circuits.
     */
    private class CycleSearch {
        /** Vertices on the current search path. */
        private val onPath = HashSet<Vertice>()

        /** Vertices whose successors have been fully searched without finding a cycle. */
        private val done = HashSet<Vertice>()

        fun reachesCycle(vertice: Vertice): Boolean {
            if (vertice in onPath) return true
            if (vertice in done) return false
            onPath.add(vertice)
            val found = vertice.getOutputs().any { output ->
                output.net?.ports
                    ?.any { it !== output && it.portType.isInput && it.owner !is GraphPort<*> && reachesCycle(it.owner!!) }
                    ?: false
            }
            onPath.remove(vertice)
            done.add(vertice)
            return found
        }
    }
}
