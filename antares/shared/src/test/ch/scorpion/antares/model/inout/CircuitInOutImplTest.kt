package ch.scorpion.antares.model.inout

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphPortTypeChanged
import ch.scorpion.jabbah.graph.model.PortType
import io.mockk.mockk
import kotlin.test.*

class CircuitInOutImplTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val inout = CircuitInOutImpl()
	private val signalHandler = mockk<SignalHandler>(relaxed = true)

	@Ignore // In work
	@Test
	fun shouldDelaySwitchOn() {
		inout.toggleBit(0, false, signalHandler)
		assertEquals(DigitalSignalFactory.of(false), inout.signal)
		assertFalse(inout.enabled)

		inout.act(signalHandler, inout.createActorData(null))
		assertEquals(DigitalSignalFactory.of(true), inout.signal)
		assertTrue(inout.enabled)
	}

	@Test
	fun shouldPostGraphPortTypeChangedEvent() {
		val eventBus = BaseModule.eventBus
		lateinit var event: GraphPortTypeChanged<*>
		eventBus.register(GraphPortTypeChanged::class) { event = it }

		inout.portType = PortType.INOUT

		assertEquals(PortType.INOUT, event.newPortType)
	}
}