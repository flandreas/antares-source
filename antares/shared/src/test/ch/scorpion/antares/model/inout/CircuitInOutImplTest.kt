package ch.scorpion.antares.model.inout

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
import ch.scorpion.jabbah.graph.model.GraphPortTypeChanged
import ch.scorpion.jabbah.graph.model.PortType
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
		inout.setIncomingSignal(Word.of(true), signalHandler)
		assertFalse(inout.enabled)

		inout.act(signalHandler, GraphActorDataImpl(null, Word.of(true)))
		assertTrue(inout.enabled)
	}

	@Test
	fun shouldNotSetSignalWhileWaiting() {
		inout.setIncomingSignal(Word.of(true), signalHandler)
		inout.setIncomingSignal(Word.of(false), signalHandler)
		assertEquals(Word.of(true), inout.signal)
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