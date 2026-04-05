package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.AbstractJvmCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.TestLibraryBuilder
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Regression test for #780 "Error message: Input port not found".
 * Occurred when reading a value from an output port.
 */
class SetOutputTest : AbstractJvmCircuitTest() {

    private lateinit var circuitView: GraphView
    private val library get() = LibraryModule.libraryHolder.library
    private lateinit var subGraphVV: SubGraphVerticeViewImpl

    override fun getCircuitView(): GraphView = circuitView

    @BeforeTest
    fun setupCircuit() {
        setupLibrary()
        TestLibraryBuilder().addScriptedBinaryFunction(
            library, "data", "I2", "address", """
                var adr = (address and 0b1)
                address = (data and 0b1) or adr
            """.trimIndent())

        subGraphVV = (library.get(TestLibraryBuilder.BINARY_FUNCTION) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
                as SubGraphVerticeViewImpl

        val builder = TestCircuitBuilder("test", styleProvider, eventBus)
        builder.addVerticeView(subGraphVV)
        circuitView = builder.build()
    }

    @Test
    fun shouldSetOutput() {
        scheduler.isDeepExecution = false
        startSimulation()
        proceedUntilQueueIsEmpty()

        subGraphVV.model.getInput<DigitalSignal>("data").setIncomingSignal(DigitalSignalFactory.of(true), scheduler)
        proceedUntilQueueIsEmpty()

        assertNoIssues()
    }
}