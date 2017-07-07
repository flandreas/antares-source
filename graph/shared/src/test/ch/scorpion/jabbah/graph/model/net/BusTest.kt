package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.execution.ForwardSignalHandler
import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.port.PortImpl
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for bus behaviour, multiple [OutputPort]s that are connected to the same [Net].
 */
class BusTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphModelTestRule()
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
        assertThat(net.signalBuffer, `is`(true))
        assertThat(net.executionError, `is`(nullValue()))
    }

    @Test
    fun shouldAcceptMultipleConsistentSignals() {
        output1.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(true, signalHandler)
        assertThat(net.signalBuffer, `is`(true))
        assertThat(net.executionError, `is`(nullValue()))
    }

    @Test
    fun shouldDetectInconsistentSignal() {
        output1.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(false, signalHandler)
        assertThat(net.executionError, `is`(not(nullValue())))
    }

    @Test
    fun shouldRecoverFromInconsistentSignal() {
        output1.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(false, signalHandler)
        output1.setOutgoingSignal(null, signalHandler)

        assertThat(net.signalBuffer, `is`(false))
        assertThat(net.executionError, `is`(nullValue()))
    }

    @Test
    fun shouldBecomeSingleUndefined() {
        output1.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(null, signalHandler)
        output1.setOutgoingSignal(null, signalHandler)

        assertThat(net.signalBuffer, `is`(nullValue()))
        assertThat(net.executionError, `is`(nullValue()))
    }

    @Test
    fun shouldBecomeAllUndefined() {
        output1.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(true, signalHandler)
        output1.setOutgoingSignal(null, signalHandler)
        output2.setOutgoingSignal(null, signalHandler)

        assertThat(net.signalBuffer, `is`(nullValue()))
        assertThat(net.executionError, `is`(nullValue()))
    }

    @Test
    fun shouldNotUndefineConsistentSignal() {
        output1.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(true, signalHandler)
        output2.setOutgoingSignal(null, signalHandler)

        assertThat(net.signalBuffer, `is`(true))
        assertThat(net.executionError, `is`(nullValue()))
    }
}