package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.graph.SignalHandlerMockBuilder
import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.PortImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [Oscilloscope]. */
class OscilloscopeTest {

	companion object {
		init {
			GraphModelTestRule.configure()
		}
	}

	private lateinit var signalHandler: SignalHandlerMockBuilder
	private lateinit var oscilloscope: Oscilloscope

	@BeforeTest
	fun setup() {
		signalHandler = SignalHandlerMockBuilder()
		oscilloscope = Oscilloscope()
	}

	@Test
	fun shouldUpdateMaxTime() {
		createPorts(1)
		oscilloscope.executionStarted(signalHandler.build())
		input("1", 100, true)
		input("1", 200, false)
		assertEquals(200L, oscilloscope.maxTime)
	}

	@Test
	fun shouldUpdateMinDiffTime() {
		createPorts(2)
		oscilloscope.executionStarted(signalHandler.build())
		input("1", 100, true)
		input("2", 150, false)
		assertEquals(50L, oscilloscope.minDiffTime)
	}

	@Test
	fun shouldUpdateFirstMinDiffTimeWithUnusedPort() {
		createPorts(2)
		oscilloscope.executionStarted(signalHandler.build())
		input("1", 100, true)
		//input("1", 150, true)
		assertEquals(Long.MAX_VALUE, oscilloscope.minDiffTime)
	}

	@Test
	fun shouldUpdateSecondMinDiffTimeWithUnusedPort() {
		createPorts(2)
		oscilloscope.executionStarted(signalHandler.build())
		input("1", 100, true)
		input("1", 150, false)
		assertEquals(Long.MAX_VALUE, oscilloscope.minDiffTime)
	}

	private fun createPorts(portsCount: Int) {
		for (i in 1..portsCount) {
			oscilloscope.addPort(PortImpl<Boolean>(portType = PortType.INPUT, name = i.toString()))
		}
	}

	private fun input(portName: String, time: Long, signal: Boolean) {
		signalHandler.withExecutionTime(time)
		oscilloscope.getInput<Boolean>(portName).setIncomingSignal(signal, signalHandler.build())
	}
}