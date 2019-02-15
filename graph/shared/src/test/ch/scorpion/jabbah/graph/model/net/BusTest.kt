package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.execution.ForwardSignalHandler
import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.Net
import kotlin.test.*

/**
 * Unit tests for bus behaviour, multiple [OutputPort]s that are connected to the same [Net].
 */
class BusTest {

    companion object {
	    init {
		    GraphModelTestRule.configure()
	    }
    }

    private val signalHandler = ForwardSignalHandler()
    private val net = NetImpl<Boolean>()
    private val output1: OutputPort<Boolean> = PortImpl.createOutput(Boolean::class)
    private val output2: OutputPort<Boolean> = PortImpl.createOutput(Boolean::class)

    init {
        net.connect(output1)
        net.connect(output2)
    }

    @Test
    fun shouldForwardSingleConsistentSignal() {
        output1.setOutgoingSignal(true, signalHandler)
        assertTrue(net.signalBuffer!!)
        assertNull(net.executionError)
    }

    @Test
    fun shouldAcceptMultipleConsistentSignals() {
        output1.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(true, signalHandler)
	    assertTrue(net.signalBuffer!!)
	    assertNull(net.executionError)
    }

    @Test
    fun shouldDetectInconsistentSignal() {
        output1.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(false, signalHandler)
        assertNotNull(net.executionError)
    }

    @Test
    fun shouldRecoverFromInconsistentSignal() {
        output1.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(false, signalHandler)
        output1.setOutgoingSignal(null, signalHandler)

        assertFalse(net.signalBuffer!!)
        assertNull(net.executionError)
    }

    @Test
    fun shouldBecomeSingleUndefined() {
        output1.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(null, signalHandler)
        output1.setOutgoingSignal(null, signalHandler)

        assertNull(net.signalBuffer)
        assertNull(net.executionError)
    }

    @Test
    fun shouldBecomeAllUndefined() {
        output1.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(true, signalHandler)
        output1.setOutgoingSignal(null, signalHandler)
        output2.setOutgoingSignal(null, signalHandler)

	    assertNull(net.signalBuffer)
	    assertNull(net.executionError)
    }

    @Test
    fun shouldNotUndefineConsistentSignal() {
        output1.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(null, signalHandler)

        assertTrue(net.signalBuffer!!)
        assertNull(net.executionError)
    }
}