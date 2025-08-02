package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.execution.SignalHandlerMockBuilder
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoriesType
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.math.max
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OscilloscopeViewTimelineTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private lateinit var signalHandler: SignalHandlerMockBuilder
	private lateinit var oscilloscope: Oscilloscope
	private lateinit var timeline: OscilloscopeViewTimeline
	private var maxTime: Long = 0

	@BeforeTest
	fun setup() {
		signalHandler = SignalHandlerMockBuilder()
		oscilloscope = Oscilloscope(SignalHistoriesType.Realtime)
		timeline = OscilloscopeViewTimeline(scale = 1.0) { maxTime }
	}

	@Test
	fun shouldCalculateForSinglePort() {
		createPorts(1)
		oscilloscope.executionInitialize(signalHandler.build())
		oscilloscope.executionStart(signalHandler.build())
		input("1", 100, true)
		input("1", 150, false)
		assertEquals(0.015, timeline.getX(0))
	}

	@Test
	fun shouldCalculateForMultiplePorts() {
		createPorts(2)
		oscilloscope.executionInitialize(signalHandler.build())
		oscilloscope.executionStart(signalHandler.build())
		input("1", 100, true)
		input("2", 110, true)
		assertEquals(0.011, timeline.getX(0))
	}

	@Test
	fun shouldIgnoreUnconnectedPort() {
		createPorts(2)
		oscilloscope.executionInitialize(signalHandler.build())
		oscilloscope.executionStart(signalHandler.build())
		input("1", 100, true)
		input("1", 150, false)
		assertEquals(0.015, timeline.getX(0))
	}

	@Test
	fun testSwitchFollowedByInverter() {
		createPorts(2)
		oscilloscope.executionInitialize(signalHandler.build())
		oscilloscope.executionStart(signalHandler.build())
		input("2", 11, true)
		input("1", 1001, false)
		input("1", 2002, true)
		input("2", 2033, false)
		assertEquals(0.2033, timeline.getX(0))
	}

	private fun createPorts(portsCount: Int) {
		for (i in 1..portsCount) {
			oscilloscope.addPort(PortImpl<Boolean>(portType = PortType.INPUT, name = i.toString()))
		}
	}

	private fun input(portName: String, time: Long, signal: Boolean) {
		maxTime = max(maxTime, time)
		signalHandler.withExecutionTime(time)
		oscilloscope.getInput<Boolean>(portName).setIncomingSignal(signal, signalHandler.build())
	}
}