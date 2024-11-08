package ch.scorpion.antares.dsl

import ch.scorpion.antares.AbstractJvmCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.TestLibraryBuilder
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
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