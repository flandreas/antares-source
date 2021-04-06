package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.execution.ForwardSignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.port.PortImpl
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
	private var net: Net<Boolean>
	private val output1 = TestVertice(canBeUndefined = true)
	private val output2 = TestVertice(canBeUndefined = true)

    init {
    	val builder = TestGraphBuilder<Boolean>()
	    builder.addVertice(output1)
	    builder.addVertice(output2)
	    net = builder.connect(output1, output1.getOutput(), output2, output2.getOutput<Boolean>(2) as PortImpl<Boolean>)
	    builder.graph.executionStarted(signalHandler)
    }

    @Test
    fun shouldForwardSingleConsistentSignal() {
        output1.getOutput<Boolean>().setOutgoingSignal(true, signalHandler)
        assertTrue(net.signalBuffer!!)
        assertNull(net.executionError)
    }

    @Test
    fun shouldAcceptMultipleConsistentSignals() {
        output1.getOutput<Boolean>().setOutgoingSignal(true, signalHandler)
        output2.getOutput<Boolean>().setOutgoingSignal(true, signalHandler)
	    assertNull(net.executionError)
    }

    @Test
    fun shouldDetectInconsistentSignal() {
        output1.getOutput<Boolean>().setOutgoingSignal(true, signalHandler)
        output2.getOutput<Boolean>().setOutgoingSignal(false, signalHandler)
        assertNotNull(net.executionError)
    }

    @Test
    fun shouldRecoverFromInconsistentSignal() {
        output1.getOutput<Boolean>().setOutgoingSignal(true, signalHandler)
        output2.getOutput<Boolean>().setOutgoingSignal(false, signalHandler)
        output1.getOutput<Boolean>().setOutgoingSignal(null, signalHandler)

        assertFalse(net.signalBuffer!!)
        assertNull(net.executionError)
    }

    @Test
    fun shouldBecomeSingleUndefined() {
        output1.getOutput<Boolean>().setOutgoingSignal(true, signalHandler)
        output2.getOutput<Boolean>().setOutgoingSignal(null, signalHandler)
        output1.getOutput<Boolean>().setOutgoingSignal(null, signalHandler)

        assertNull(net.signalBuffer)
        assertNull(net.executionError)
    }

    @Test
    fun shouldBecomeAllUndefined() {
        output1.getOutput<Boolean>().setOutgoingSignal(true, signalHandler)
        output2.getOutput<Boolean>().setOutgoingSignal(true, signalHandler)
        output1.getOutput<Boolean>().setOutgoingSignal(null, signalHandler)
        output2.getOutput<Boolean>().setOutgoingSignal(null, signalHandler)

	    assertNull(net.signalBuffer)
	    assertNull(net.executionError)
    }

    @Test
    fun shouldNotUndefineConsistentSignal() {
        output1.getOutput<Boolean>().setOutgoingSignal(true, signalHandler)
        output2.getOutput<Boolean>().setOutgoingSignal(true, signalHandler)
        output2.getOutput<Boolean>().setOutgoingSignal(null, signalHandler)

        assertTrue(net.signalBuffer!!)
        assertNull(net.executionError)
    }
}