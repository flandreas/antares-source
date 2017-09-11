package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.execution.SignalHandlerMockBuilder
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.PortImpl
import org.junit.Assert.*
import org.junit.Before
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

/** Unit tests for [Oscilloscope]. */
class OscilloscopeTest {

    private lateinit var signalHandler: SignalHandlerMockBuilder
    private lateinit var oscilloscope: Oscilloscope

    @Before
    fun setup() {
        BaseModuleJvm.require()
        signalHandler = SignalHandlerMockBuilder()
        oscilloscope = Oscilloscope()
    }

    @Test
    fun shouldUpdateMaxTime() {
        createPorts(1)
        oscilloscope.executionStarted(signalHandler.build())
        input("1", 100, true)
        input("1", 200, false)
        assertThat(oscilloscope.maxTime, `is`(200L))
    }

    @Test
    fun shouldUpdateMinDiffTime() {
        createPorts(2)
        oscilloscope.executionStarted(signalHandler.build())
        input("1", 100, true)
        input("2", 150, false)
        assertThat(oscilloscope.minDiffTime, `is`(50L))
    }

    @Test
    fun shouldUpdateFirstMinDiffTimeWithUnusedPort() {
        createPorts(2)
        oscilloscope.executionStarted(signalHandler.build())
        input("1", 100, true)
        //input("1", 150, true)
        assertThat(oscilloscope.minDiffTime, `is`(Long.MAX_VALUE))
    }

    @Test
    fun shouldUpdateSecondMinDiffTimeWithUnusedPort() {
        createPorts(2)
        oscilloscope.executionStarted(signalHandler.build())
        input("1", 100, true)
        input("1", 150, false)
        assertThat(oscilloscope.minDiffTime, `is`(Long.MAX_VALUE))
    }

    private fun createPorts(portsCount: Int) {
        for (i in 1..portsCount) {
            oscilloscope.addPort(PortImpl<Boolean>(portType = PortType.INPUT, name=i.toString()))
        }
    }

    private fun input(portName: String, time: Long, signal: Boolean) {
        signalHandler.withExecutionTime(time)
        oscilloscope.getInput<Boolean>(portName).setIncomingSignal(signal, signalHandler.build())
    }
}