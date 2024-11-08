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
import junit.framework.TestCase.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Regression test for #780 "Error message: Input port not found".
 * Bit accessors read the old value before setting a bit, which didn't
 * work for output ports: The logic could only read values from input ports.
 */
class SetOutputBitTest : AbstractJvmCircuitTest() {

    private lateinit var circuitView: GraphView
    private val library get() = LibraryModule.libraryHolder.library
    private lateinit var subGraphVV: SubGraphVerticeViewImpl

    override fun getCircuitView(): GraphView = circuitView

    @BeforeTest
    fun setupCircuit() {
        setupLibrary()
        TestLibraryBuilder().addScriptedBinaryFunction(
            library, "I1", "I2", "O", "O@0 = I1@0")

        subGraphVV = (library.get(TestLibraryBuilder.BINARY_FUNCTION) as LibraryElement)
            .getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeViewImpl

        val builder = TestCircuitBuilder("test", styleProvider, eventBus)
        builder.addVerticeView(subGraphVV)
        circuitView = builder.build()
    }

    @Test
    fun shouldSetOutputBit() {
        scheduler.isDeepExecution = false
        startSimulation()
        proceedUntilQueueIsEmpty()

        subGraphVV.model.getInput<DigitalSignal>("I1").setIncomingSignal(DigitalSignalFactory.of(true), scheduler)
        proceedUntilQueueIsEmpty()

        assertNoIssues()
        assertEquals(DigitalSignalFactory.of(true), subGraphVV.model.getOutput<DigitalSignal>().getOutgoingSignal())
    }
}