package io.antarescircuit.antares.view.inout

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.model.StoringGraphActorData
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class CircuitInOutViewTest {

	private val signalHandler = mock<SignalHandler>(MockMode.autofill)

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldNotChangeLocationWhenChangingBitWidthWhileConnected() {
		val builder = TestCircuitBuilder("test")
		val input1 = builder.addInput()
		val input2 = builder.addInput()
		input1.location = Point2D.ZERO
		input2.location = Point2D(0, 100)
		builder.connectOutputOpen(input1, Point2D(100, 0))

		input1.bitWidth = BitWidth.BW_2
		input2.bitWidth = BitWidth.BW_2

		assertEquals(Point2D.ZERO, input1.location)
		assertEquals(input2.boundingBox.minX, input1.boundingBox.minX)
	}

	@Test
	fun shouldNotConsumeUndefinedDecimalDigit() {
		val input = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(bitWidth = BitWidth.BW_8))
		input.signalRepresentation = DigitalSignalRepresentation.DECIMAL
		input.focusGained()
		input.consumeKey(KeyEvent.VK_Z, GraphApplicationContextHolder(mock(MockMode.autofill)), skipAnimation = true)
	}

	@Test
	fun shouldClearByUser() {
		val input = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(bitWidth = BitWidth.BW_8))
		input.signalRepresentation = DigitalSignalRepresentation.DECIMAL
		input.focusGained()
		input.model.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 42), signalHandler)

		input.clearByUser(signalHandler)
		input.model.act(signalHandler, StoringGraphActorData(null, DigitalSignalFactory.of(BitWidth.BW_8, 0)))

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 0), input.model.signal)
	}
}