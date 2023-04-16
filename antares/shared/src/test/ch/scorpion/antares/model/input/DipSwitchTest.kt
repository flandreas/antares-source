package ch.scorpion.antares.model.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.False
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_4
import ch.scorpion.antares.model.signal.DigitalSignalFactory.allOf
import ch.scorpion.antares.model.signal.DigitalSignalFactory.of
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DipSwitchTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val signalHandler: SignalHandler = mockk(relaxed = true)
	private val graphView = mockk<GraphView>(relaxed = true)

	@Test
	fun shouldDelayChangeInSetBit() {
		val dipSwitch = DipSwitch()
		dipSwitch.executionInitialize(signalHandler)
		dipSwitch.setBit(0, Bit.True, signalHandler, graphView)
		assertFalse(dipSwitch.enabled)
		assertEquals(allOf(BW_4, False), dipSwitch.signal)

		dipSwitch.act(signalHandler, dipSwitch.createActorData(null))
		assertTrue(dipSwitch.enabled)
		assertEquals(of(BW_4, 1), dipSwitch.signal)
	}

	@Test
	fun shouldDelayChangeInSetValue() {
		val dipSwitch = DipSwitch()
		dipSwitch.executionInitialize(signalHandler)
		dipSwitch.setValue(of(BW_4, 3), signalHandler, graphView)
		assertFalse(dipSwitch.enabled)
		assertEquals(allOf(BW_4, False), dipSwitch.signal)

		dipSwitch.act(signalHandler, dipSwitch.createActorData(null))
		assertTrue(dipSwitch.enabled)
		assertEquals(of(BW_4, 3), dipSwitch.signal)
	}
}