package io.antarescircuit.jabbah.graph.model.oscilloscope

import io.antarescircuit.jabbah.execution.SignalHandlerMockBuilder
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignalHistoriesTest {

	@Test
	fun shouldResetRealtimeMaximumWhenCleared() {
		val histories = RealtimeSignalHistories(createOscilloscope())
		val handler = SignalHandlerMockBuilder().withExecutionTime(1_000).build()
		histories.storeSignal("clock", true, handler)
		assertEquals(1_000L, histories.maxTime)
		histories.clear()
		assertEquals(0L, histories.maxTime)
		assertTrue(histories.getSignalHistory("clock")!!.isEmpty)
	}

	@Test
	fun shouldRestartClockedRecordingAtZeroAfterClearing() {
		val histories = ClockedSignalHistories(createOscilloscope())
		val handler = SignalHandlerMockBuilder().build()
		histories.storeSignal("clock", true, handler)
		histories.storeSignal("clock", false, handler)
		assertEquals(200L, histories.maxTime)
		histories.clear()
		assertEquals(0L, histories.maxTime)
		histories.storeSignal("clock", true, handler)
		assertEquals(0L, histories.getSignalHistory("clock")!!.last().time)
		assertEquals(100L, histories.maxTime)
	}

	private fun createOscilloscope(): Oscilloscope {
		GraphViewTestRule.configure()
		return Oscilloscope().also {
			it.addPort(GraphModelModule.portFactory.createOscilloscopeProbePort<Any>("clock", GenericGraphType))
		}
	}
}
