package io.antarescircuit.antares

import io.antarescircuit.antares.model.gate.NonUnaryLogicGate
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.param.GraphParamValue
import io.antarescircuit.jabbah.graph.model.param.LongValueGraphParamType
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PropagationDelayExpressionIntegrationTest : AbstractJvmCircuitTest() {

    private lateinit var circuitView: GraphView
    private val library get() = LibraryModule.libraryHolder.library
    private lateinit var subGraphVV: SubGraphVerticeViewImpl

    override fun getCircuitView(): GraphView = circuitView

    @BeforeTest
    fun setupCircuit() {
        setupLibrary()
        TestLibraryBuilder().addPropagationDelayExpressionOrGate(library, "PD", "PD * 2")

        subGraphVV = (library.get(TestLibraryBuilder.PROPAGATION_DELAY_EXPRESSION) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
                as SubGraphVerticeViewImpl

        val builder = TestCircuitBuilder("test", styleProvider, eventBus)
        builder.addVerticeView(subGraphVV)
        circuitView = builder.build()
    }

    @Test
    fun shouldUpdateCircuit() {
        val gate = subGraphVV.model.getGraph().elements.first() as NonUnaryLogicGate

        subGraphVV.model.setParamValue(GraphParamValue.create("PD", LongValueGraphParamType, LongValueImpl(25L), null))

        assertEquals(2 * 25, gate.propagationDelay.value)
    }
}