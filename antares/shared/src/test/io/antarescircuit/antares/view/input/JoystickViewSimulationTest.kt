package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.graph.view.GraphView
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class JoystickViewSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var joystickView: JoystickView

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		joystickView = builder.addVerticeView(JoystickView())
		joystickView.moveBy(-joystickView.bounds.centerX, -joystickView.bounds.centerY)
		circuitView = builder.build()
	}

	@Test
	fun shouldCalculateRectangularDeflection() {
		startSimulation()
		proceedUntilQueueIsEmpty()
		pressMouseAt(0, 0)

		dragMouseTo(100, 0)
		assertOutput(3, 2)

		dragMouseTo(100, -100)
		assertOutput(3, 1)

		dragMouseTo(0, -100)
		assertOutput(2, 1)

		dragMouseTo(-100, -100)
		assertOutput(1, 1)

		dragMouseTo(-100, 0)
		assertOutput(1, 2)

		dragMouseTo(-100, 100)
		assertOutput(1, 3)

		dragMouseTo(0, 100)
		assertOutput(2, 3)

		dragMouseTo(100, 100)
		assertOutput(3, 3)

		releaseMouseAt(100, 100)
		assertOutput(2, 2)
	}

	private fun assertOutput(x: Int, y: Int) {
		assertEquals(DigitalSignalFactory.of(BitWidth.BW_2, x.toLong()), joystickView.model.getOutput<DigitalSignal>("X").getOutgoingSignal())
		assertEquals(DigitalSignalFactory.of(BitWidth.BW_2, y.toLong()), joystickView.model.getOutput<DigitalSignal>("Y").getOutgoingSignal())
	}

	private fun pressMouseAt(x: Int, y: Int) {
		val event = MouseEventImpl(
			type = MouseEventType.PRESSED,
			button = Button.BUTTON1,
			x = x,
			y = y)
		val context = contextFor(event)
		joystickView.getActorInteractionHandler(context).mousePressed(context)
		proceedUntilQueueIsEmpty()
	}

	private fun dragMouseTo(x: Int, y: Int) {
		val event = MouseEventImpl(
			type = MouseEventType.DRAGGED,
			button = Button.BUTTON1,
			x = x,
			y = y)
		val context = contextFor(event)
		joystickView.getActorInteractionHandler(context).mouseDragged(context)
		proceedUntilQueueIsEmpty()
	}

	private fun releaseMouseAt(x: Int, y: Int) {
		val event = MouseEventImpl(
			type = MouseEventType.RELEASED,
			button = Button.BUTTON1,
			x = x,
			y = y)
		val context = contextFor(event)
		joystickView.getActorInteractionHandler(context).mouseReleased(context)
		proceedUntilQueueIsEmpty()
	}

	private fun contextFor(mouseEvent: MouseEvent): ActorInteractionContext {
		return ActorInteractionContext(
			signalHandler = scheduler,
			view = mock(MockMode.autofill),
			mouseEvent = mouseEvent,
			keyEvent = null,
			x = mouseEvent.x.toDouble(),
			y = mouseEvent.y.toDouble())
	}
}