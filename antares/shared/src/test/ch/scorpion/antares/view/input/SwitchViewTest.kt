package ch.scorpion.antares.view.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SwitchViewTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val switchView = SwitchView()
	private val signalHandler: SignalHandler = mockk(relaxed = true)

	@Test
	fun shouldConsumeKeyEventForName() {
		val keyEvent = keyEvent('A'.code)
		val context = contextFor(keyEvent = keyEvent)
		switchView.name = "A"
		switchView.getActorInteractionHandler(context).keyPressed(context)

		verify { keyEvent.consumeEvent() }
	}

	@Test
	fun shouldNotConsumeKeyEventForOtherName() {
		val keyEvent = keyEvent('B'.code)
		val context = contextFor(keyEvent = keyEvent)
		switchView.name = "A"
		switchView.getActorInteractionHandler(context).keyPressed(context)

		verify(exactly = 0) { keyEvent.consumeEvent() }
	}

	@Test
	fun shouldToggleWithMousePress() {
		switchView.toggle = false
		pressMouseButton()

		// State change is deferred
		assertFalse(switchView.model.isOn)

		act()
		assertTrue(switchView.model.isOn)
	}

	@Test
	fun shouldNotToggle() {
		switchView.toggle = false
		pressMouseButton()
		assertFalse(switchView.model.isOn)
		assertFalse(switchView.model.enabled)
		act()
		assertTrue(switchView.model.isOn)
		assertTrue(switchView.model.enabled)

		releaseMouseButton()
		assertTrue(switchView.model.isOn)
		assertFalse(switchView.model.enabled)
		act()
		assertFalse(switchView.model.isOn)
		assertTrue(switchView.model.enabled)
	}

	@Test
	fun shouldNotForgetReleaseWhenNotToggling() {
		switchView.toggle = false
		pressMouseButton()
		// No act
		releaseMouseButton()
		act()
		act()
		assertFalse(switchView.model.isOn)
		assertTrue(switchView.model.enabled)
	}

	@Test
	fun shouldNotToggleWithReleaseWithoutPrecedingPress() {
		switchView.toggle = false
		pressMouseButton()

		assertFalse(switchView.model.isOn)
	}

	@Test
	fun shouldNotToggleWithRightMouseButton() {
		switchView.toggle = false
		pressMouseButton(Button.BUTTON3)

		assertFalse(switchView.model.isOn)
	}

	private fun pressMouseButton(button: Button = Button.BUTTON1) {
		val event = MouseEventImpl(MouseEventType.PRESSED, button = button)
		val context = contextFor(mouseEvent = event)
		switchView.getActorInteractionHandler(context).mousePressed(context)
	}

	private fun releaseMouseButton(button: Button = Button.BUTTON1) {
		val event = MouseEventImpl(MouseEventType.RELEASED, button = button)
		val context = contextFor(mouseEvent = event)
		switchView.getActorInteractionHandler(context).mouseReleased(context)
	}

	private fun act() {
		switchView.model.act(signalHandler, switchView.model.createActorData(null))
	}

	private fun keyEvent(key: Int): KeyEvent {
		val keyEvent: KeyEvent = mockk(relaxed = true)
		every { keyEvent.key } returns key
		every { keyEvent.modifiers } returns 0
		return keyEvent
	}

	private fun contextFor(mouseEvent: MouseEvent? = null, keyEvent: KeyEvent? = null): ActorInteractionContext {
		return ActorInteractionContext(
			signalHandler = mockk(relaxed = true),
			view = mockDrawingView(),
			mouseEvent = mouseEvent,
			keyEvent = keyEvent,
			x = 0.0,
			y = 0.0)
	}

	private fun mockDrawingView(): DrawingView<*> {
		val drawingView = mockk<DrawingView<*>>(relaxed = true)
		every { drawingView.drawing } returns mockk<GraphView>(relaxed = true)
		return drawingView
	}
}