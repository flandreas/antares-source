package io.antarescircuit.antares.standardlibrary

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.graph.GraphPropagationDelayCalculator
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
        val builder = TestCircuitBuilder("reconverging")
        fun gate() = builder.addVerticeView(LogicGateView.andGateView()).also { it.vertice.propagationDelay = LongValueImpl(1) }

        var previous: VerticeView<*> = builder.addInput()
        repeat(40) {
            // Two parallel gates merging into a third one
            val a = gate()
            val b = gate()
            val merge = gate()
            builder.connect(previous, a, a.vertice.getInput(1))
            // The builder creates one net per connection, so fan out by connecting to the existing net
            previous.vertice.getOutput<DigitalSignal>().net!!.connect(b.vertice.getInput<DigitalSignal>(1))
            builder.connect(a, merge, merge.vertice.getInput(1))
            builder.connect(b, merge, merge.vertice.getInput(2))
            previous = merge
        }
        builder.connect(previous, builder.addOutput())

        val delay = GraphPropagationDelayCalculator().calculate(builder.graph)

        assertEquals(80L, delay)
    }
}
