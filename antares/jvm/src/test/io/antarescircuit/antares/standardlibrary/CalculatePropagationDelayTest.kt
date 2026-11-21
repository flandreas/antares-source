package io.antarescircuit.antares.standardlibrary

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.graph.GraphPropagationDelayCalculator
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Execute [GraphPropagationDelayCalculator] on some standard library circuits.
 */
class CalculatePropagationDelayTest {

    @BeforeTest
    fun setup() {
        AntaresTestRule.configure()
        AbstractStandardLibraryBasedCircuitTest.setupLibrary()
    }

    @Test
    fun shouldCalculateSRLatch() {
        val metaGraph = LibraryModule.libraryHolder.library.getMetaGraph(UUID("e57241a4-0282-4006-9abd-59bbcb16bb87"))

        val delay = GraphPropagationDelayCalculator().calculate(metaGraph.graph.model!!)

        assertEquals(39L, delay)
    }

    /** Each stage doubles the number of paths, which made enumerating them hang when saving large circuits. */
    @Test
    fun shouldCalculateManyReconvergingPaths() {
        assertEquals(80L, GraphPropagationDelayCalculator().calculate(reconvergingStages(feedback = false)))
    }

    /** Like [shouldCalculateManyReconvergingPaths], but all paths are inside one loop, like memory cells on a bus. */
    @Test
    fun shouldCalculateLargeLoopsInBoundedTime() {
        val delay = GraphPropagationDelayCalculator().calculate(reconvergingStages(feedback = true))

        assertTrue(delay >= 80L, "at least the path without feedback: $delay")
    }

    /** 40 stages of two parallel gates merging into a third one, optionally fed back to the first stage. */
    private fun reconvergingStages(feedback: Boolean): Graph {
        val builder = TestCircuitBuilder("reconverging")
        fun gate() = builder.addVerticeView(LogicGateView.andGateView()).also { it.vertice.propagationDelay = LongValueImpl(1) }

        var previous: VerticeView<*> = builder.addInput()
        var first: VerticeView<*>? = null
        repeat(40) {
            // Two parallel gates merging into a third one
            val a = gate()
            val b = gate()
            val merge = gate()
            builder.connect(previous, a, a.vertice.getInput(1))
            if (first == null) first = a
            // The builder creates one net per connection, so fan out by connecting to the existing net
            previous.vertice.getOutput<DigitalSignal>().net!!.connect(b.vertice.getInput<DigitalSignal>(1))
            builder.connect(a, merge, merge.vertice.getInput(1))
            builder.connect(b, merge, merge.vertice.getInput(2))
            previous = merge
        }
        builder.connect(previous, builder.addOutput())
        if (feedback) previous.vertice.getOutput<DigitalSignal>().net!!.connect(first!!.vertice.getInput<DigitalSignal>(2))
        return builder.graph
    }

    /**
     * A and B form a loop, which is cut differently depending on where it is entered: the path D-C-B-A is longer than
     * D-A-B. Reusing the delay of B calculated with A on the path gave 3.
     */
    @Test
    fun shouldNotReuseDelaysCutByALoop() {
        val builder = TestCircuitBuilder("loop")
        fun gate() = builder.addVerticeView(LogicGateView.andGateView()).also { it.vertice.propagationDelay = LongValueImpl(1) }
        fun fanOut(from: VerticeView<*>, to: VerticeView<*>, input: Int) =
            from.vertice.getOutput<DigitalSignal>().net!!.connect(to.vertice.getInput<DigitalSignal>(input))

        val a = gate()
        val b = gate()
        val c = gate()
        val d = gate()
        builder.connect(builder.addInput(), a, a.vertice.getInput(1))
        builder.connect(b, a, a.vertice.getInput(2))
        builder.connect(a, b, b.vertice.getInput(1))
        fanOut(b, c, 1)
        // D visits A first
        fanOut(a, d, 1)
        builder.connect(c, d, d.vertice.getInput(2))
        builder.connect(d, builder.addOutput())

        val delay = GraphPropagationDelayCalculator().calculate(builder.graph)

        assertEquals(4L, delay)
    }
}
