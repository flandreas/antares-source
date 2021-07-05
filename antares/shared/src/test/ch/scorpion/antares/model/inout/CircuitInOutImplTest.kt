package ch.scorpion.antares.model.inout

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.GraphPortTypeChanged
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CircuitInOutImplTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val inout = CircuitInOutImpl()
	private val signalHandler = mockk<SignalHandler>(relaxed = true)

	@Test
	fun shouldBeDisabledWhileWaiting() {
		inout.setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		assertFalse(inout.enabled)

		inout.act(signalHandler, GraphActorDataImpl(null, DigitalSignalFactory.of(true)))
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