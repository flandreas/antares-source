package ch.scorpion.antares.dsl

import ch.scorpion.antares.AbstractJvmCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.TestLibraryBuilder
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
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