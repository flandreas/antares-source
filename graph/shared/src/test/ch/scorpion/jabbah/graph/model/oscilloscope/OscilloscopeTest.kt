package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.SignalHandlerMockBuilder
import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.PortImpl
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [Oscilloscope]. */
class OscilloscopeTest {

	companion object {
		init {
			GraphModelTestRule.configure()
		}
	}

	private val signalHandler = SignalHandlerMockBuilder()
	private val oscilloscope = Oscilloscope()

	@Test
	fun shouldUpdateMaxTime() {
		createPorts(1)
		oscilloscope.executionInitialize(signalHandler.build())
		oscilloscope.executionStart(signalHandler.build())
		input("1", 100, true)
		input("1", 200, false)
		assertEquals(200L, oscilloscope.maxTime)
	}

	@Test
	fun shouldTruncateEntries() {
		BaseModule.properties.customize(Oscilloscope.PROP_BUFFER_SIZE, 2)
		createPorts(1)
		oscilloscope.executionInitialize(signalHandler.build())
		oscilloscope.executionStart(signalHandler.build())
		input("1", 100, true)
		input("1", 200, false)
		input("1", 300, true)

		assertEquals(2, oscilloscope.getSignalHistory("1")!!.size)
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