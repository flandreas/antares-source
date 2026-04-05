package io.antarescircuit.jabbah.graph.model.graph

import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.graph.model.TestVertice
import io.antarescircuit.jabbah.graph.model.vertice.GraphInputImpl
import io.antarescircuit.jabbah.graph.model.vertice.GraphOutputImpl
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.TestGraphPortView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphPropagationDelayCalculatorTest {

    @BeforeTest
    fun setup() {
        GraphViewTestRule.configure()
        Translations.withAnyKey()
    }

    @Test
    fun shouldCalculate() {
        val builder = GraphViewBuilder<Boolean>()

        val input = builder.addVerticeView(TestGraphPortView<Boolean>(model = GraphInputImpl()))
        val v1 = builder.addVerticeView(TestVerticeView(vertice = TestVertice().apply { propagationDelay = LongValueImpl(100) }))
        val v2 = builder.addVerticeView(TestVerticeView(vertice = TestVertice().apply { propagationDelay = LongValueImpl(200) }))
        val output = builder.addVerticeView(TestGraphPortView<Boolean>(model = GraphOutputImpl()))
        builder.connect(input, v1)
        builder.connect(v1, v2)
        builder.connect(v2, output)

        val delay = GraphPropagationDelayCalculator().calculate(builder.graph)

        assertEquals(300L, delay)
    }
}