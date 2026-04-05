package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.AbstractJvmCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.TestLibraryBuilder
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the "triggerAfter()" function of [AntaresDslGlobalFunctions].
 */
class TriggerAfterTest : AbstractJvmCircuitTest() {

    private lateinit var circuitView: GraphView
    private val library get() = LibraryModule.libraryHolder.library
    private lateinit var subGraphVV: SubGraphVerticeViewImpl

    override fun getCircuitView(): GraphView = circuitView

    private val outputSignal: DigitalSignal get() = subGraphVV.model.getOutput<DigitalSignal>().getOutgoingSignal()!!

    @BeforeTest
    fun setupCircuit() {
        setupLibrary()
        TestLibraryBuilder().addScriptedBinaryFunction(
            library, "I1", "I2", "O",
            script = """
                init {
                    store waiting
                    waiting = 0
                }
                if (waiting == 0) {
                    O = 1
                    waiting = 1
                    triggerAfter(1000)
                } else {
                    O = 0
                    waiting = 0
                }
                
            """.trimIndent()
        )

        subGraphVV = (library.get(TestLibraryBuilder.BINARY_FUNCTION) as LibraryElement)
            .getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeViewImpl
        subGraphVV.propagationDelay = LongValueImpl(100)

        val builder = TestCircuitBuilder("test", styleProvider, eventBus)
        builder.addVerticeView(subGraphVV)
        circuitView = builder.build()
    }

    @Test
    fun shouldTriggerAfter() {
        scheduler.isDeepExecution = false
        startSimulation()

        proceedToNanos(100)
        assertEquals(DigitalSignalFactory.of(true), outputSignal)

        proceedToNanos(1000)
        assertEquals(DigitalSignalFactory.of(true), outputSignal)

        proceedToNanos(1200)
        assertEquals(DigitalSignalFactory.of(false), outputSignal)
    }
}