package io.antarescircuit.antares

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CircuitInputStartValueTest : AbstractJvmCircuitTest() {

    private lateinit var circuitView: GraphView
    private val library get() = LibraryModule.libraryHolder.library
    private lateinit var subGraphVV: SubGraphVerticeViewImpl

    override fun getCircuitView(): GraphView = circuitView

    @BeforeTest
    fun setupCircuit() {
        setupLibrary()
        TestLibraryBuilder().addNOP(library, inputStartValue = DigitalSignalFactory.of(Bit.True))

        subGraphVV = (library.get(TestLibraryBuilder.NOP) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
            as SubGraphVerticeViewImpl

        val builder = TestCircuitBuilder("test", styleProvider, eventBus)
        builder.addVerticeView(subGraphVV)
        circuitView = builder.build()
    }

    @Test
    fun shouldApplyStartValue() {
        startSimulation()
        proceedUntilQueueIsEmpty()
        assertEquals(DigitalSignalFactory.of(Bit.True), subGraphVV.model.getOutput<DigitalSignal>().getOutgoingSignal())
    }
}