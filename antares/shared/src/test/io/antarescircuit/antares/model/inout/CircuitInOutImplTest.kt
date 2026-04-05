package io.antarescircuit.antares.model.inout

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphPortTypeChanged
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.StoringGraphActorData
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CircuitInOutImplTest {

	private val inout: DigitalCircuitInOutImpl
	private val signalHandler = mock<SignalHandler>(MockMode.autofill)

	init {
		AntaresTestRule.configure()
		inout = DigitalCircuitInOutImpl()
	}

	@Test
	fun shouldDelaySwitchOn() {
		inout.toggleBit(0, false, signalHandler)
		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_1), inout.signal)
		assertFalse(inout.enabled)

		inout.act(signalHandler, StoringGraphActorData(null, DigitalSignalFactory.of(true)))
		assertEquals(DigitalSignalFactory.of(true), inout.signal)
		assertTrue(inout.enabled)
	}

	@Test
	fun shouldDirectlyHandleInputChange() {
		inout.portType = PortType.OUTPUT
		inout.setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)

		inout.act(signalHandler, StoringGraphActorData(null, DigitalSignalFactory.of(true)))
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