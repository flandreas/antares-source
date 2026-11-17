package io.antarescircuit.jabbah.graph.model.graph

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.graph.model.*

/**
 * Calculates the maximum of the sum of all propagation delays when traversing a [Graph]
 * from any [InputPort] to any [OutputPort].
 *
 * Feedback loops are cut where the traversal reaches a [Vertice] already on the current path, so the result is the
 * longest path that visits each [Vertice] at most once.
 *
 * Enumerating all paths grows exponentially with the number of fan-outs. Therefore the vertices are first grouped into
 * feedback loops (strongly connected components). The delay of a [Vertice] entered from outside its loop doesn't
 * depend on the path that led there, so it is calculated once. Only paths inside a loop are enumerated.
 *
 * Finding the longest path inside a loop is still exponential (it is NP-hard), e.g. for the loop formed by all
 * memory cells on a data bus. If a loop takes more than [LOOP_STEPS] steps, the delays of its vertices are reused
 * like outside loops, wherever they were cut. The result is then an estimate combining paths cut at different
 * vertices of the loop.
 */
class GraphPropagationDelayCalculator {

    companion object {
        /** The name of the [Boolean] property in [Properties] calculation upon save.*/
        const val PROP_CALCULATE_ON_SAVE = "graph.model.graph.calculatePropDelayOnSave"

        /** The number of steps after which the paths through a loop are no longer enumerated. */
        internal const val LOOP_STEPS = 10_000
    }

    /** The longest delay from a graph input up to (and including) a [Vertice], when entered from outside its loop. */
    private val delays = mutableMapOf<Vertice, Long>()

    /** The vertices whose inputs are being traversed. */
    private val path = mutableSetOf<Vertice>()

    /** The feedback loop of every [Vertice], see [FeedbackLoops]. */
    private var loops = FeedbackLoops(emptyMap())

    /** The number of vertices on [path] per loop. */
    private val loopsOnPath = mutableMapOf<Int, Int>()

    /** The number of steps enumerating paths inside each loop, see [LOOP_STEPS]. */
    private val loopSteps = mutableMapOf<Int, Int>()

    /**
     * @return -1 if no [InputPort]s exist in the [Graph].
     */
    fun calculate(graph: Graph): Long {
        delays.clear()
        loopSteps.clear()
        loops = FeedbackLoops.of(graph.elements.filterIsInstance<Vertice>(), ::drivers)
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
        val loop = loops[vertice]
        path.add(vertice)
        loopsOnPath[loop] = (loopsOnPath[loop] ?: 0) + 1
        val maxValue = drivers(net).maxOfOrNull { calculateBackwardsFromVertice(it) } ?: 0L
        loopsOnPath[loop] = loopsOnPath[loop]!! - 1
        path.remove(vertice)
        return vertice.propagationDelay.value + maxValue
    }

    private fun calculateBackwardsFromVertice(vertice: Vertice): Long {
        val loop = loops[vertice]
        // Delays of vertices entered from outside their loop are complete, since the loop is cut nowhere yet
        val isEnteringLoop = (loopsOnPath[loop] ?: 0) == 0
        val isApproximated = !isEnteringLoop && countLoopStep(loop) > LOOP_STEPS
        if (isEnteringLoop || isApproximated) delays[vertice]?.let { return it }
        val isOnPath = vertice in path
        val result = vertice.getInputs()
            .maxOfOrNull { calculateBackwardsFromInputPort(vertice, it) }
            ?: 0L
        // The delay of a vertice on the path is 0, since the loop is cut there
        if (isEnteringLoop || (isApproximated && !isOnPath)) delays[vertice] = result
        return result
    }

    private fun countLoopStep(loop: Int): Int = ((loopSteps[loop] ?: 0) + 1).also { loopSteps[loop] = it }

    private fun drivers(net: Net<*>): List<Vertice> = net.ports
        .filter { it.portType.isOutput }
        .mapNotNull { it.owner }
        .filter { it !is GraphPort<*> }

    private fun drivers(vertice: Vertice): List<Vertice> = vertice.getInputs()
        .mapNotNull { it.net }
        .flatMap { drivers(it) }
}

/**
 * Groups vertices into feedback loops, the strongly connected components of the graph formed by their drivers.
 * A [Vertice] not in a loop gets a loop of its own.
 */
internal class FeedbackLoops(private val loopIds: Map<Vertice, Int>) {

    operator fun get(vertice: Vertice): Int = loopIds[vertice] ?: -1

    companion object {

        /** Uses Tarjan's algorithm, iteratively to support long chains of vertices. */
        fun of(vertices: Collection<Vertice>, drivers: (Vertice) -> List<Vertice>): FeedbackLoops {
            val index = mutableMapOf<Vertice, Int>()
            val lowLink = mutableMapOf<Vertice, Int>()
            val stack = ArrayDeque<Vertice>()
            val onStack = mutableSetOf<Vertice>()
            val loopIds = mutableMapOf<Vertice, Int>()
            var nextIndex = 0
            var nextLoopId = 0

            for (root in vertices) {
                if (root in index) continue
                // Each frame is a vertice and an iterator over its drivers still to visit
                val frames = ArrayDeque<Pair<Vertice, Iterator<Vertice>>>()
                fun visit(vertice: Vertice) {
                    index[vertice] = nextIndex
                    lowLink[vertice] = nextIndex
                    nextIndex++
                    stack.addLast(vertice)
                    onStack.add(vertice)
                    frames.addLast(vertice to drivers(vertice).iterator())
                }
                visit(root)
                while (frames.isNotEmpty()) {
                    val (vertice, iterator) = frames.last()
                    if (iterator.hasNext()) {
                        val driver = iterator.next()
                        if (driver !in index) {
                            visit(driver)
                        } else if (driver in onStack) {
                            lowLink[vertice] = minOf(lowLink[vertice]!!, index[driver]!!)
                        }
                        continue
                    }
                    frames.removeLast()
                    frames.lastOrNull()?.let { (parent, _) ->
                        lowLink[parent] = minOf(lowLink[parent]!!, lowLink[vertice]!!)
                    }
                    if (lowLink[vertice] == index[vertice]) {
                        do {
                            val member = stack.removeLast()
                            onStack.remove(member)
                            loopIds[member] = nextLoopId
                        } while (member !== vertice)
                        nextLoopId++
                    }
                }
            }
            return FeedbackLoops(loopIds)
        }
    }
}
