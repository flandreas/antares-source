package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.SignalHandlerMockBuilder
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope
import ch.scorpion.jabbah.graph.model.port.PortImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OscilloscopeViewTimelineTest {

	companion object {
		init {
			BaseModule.require()
			Translations.withAnyKey()
		}
	}

	private lateinit var signalHandler: SignalHandlerMockBuilder
	private lateinit var oscilloscope: Oscilloscope
	private lateinit var timeline: OscilloscopeViewTimeline

	@BeforeTest
	fun setup() {
		signalHandler = SignalHandlerMockBuilder()
		oscilloscope = Oscilloscope()
		timeline = OscilloscopeViewTimeline(scale = 1.0, model = oscilloscope)
	}

	@Test
	fun shouldCalculateForSinglePort() {
		createPorts(1)
		oscilloscope.executionStarted(signalHandler.build())
		input("1", 100, true)
		input("1", 150, false)
		assertEquals(7.5, timeline.getX(0))
	}

	@Test
	fun shouldCalculateForMultiplePorts() {
		createPorts(2)
		oscilloscope.executionStarted(signalHandler.build())
		input("1", 100, true)
		input("2", 110, true)
		assertEquals(5.5, timeline.getX(0))
	}

	@Test
	fun shouldIgnoreUnconnectedPort() {
		createPorts(2)
		oscilloscope.executionStarted(signalHandler.build())
		input("1", 100, true)
		input("1", 150, false)
		assertEquals(7.5, timeline.getX(0))
	}

	@Test
	fun testSwitchFollowedByInverter() {
		createPorts(2)
		oscilloscope.executionStarted(signalHandler.build())
		input("2", 11, true)
		input("1", 1001, false)
		input("1", 2002, true)
		input("2", 2033, false)
		assertEquals(101.65, timeline.getX(0))
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