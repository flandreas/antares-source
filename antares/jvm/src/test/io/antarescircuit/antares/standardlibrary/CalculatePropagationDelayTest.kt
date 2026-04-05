package io.antarescircuit.antares.standardlibrary

import io.antarescircuit.antares.AntaresTestRule
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
}