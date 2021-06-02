package ch.scorpion.antares.view.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
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

	@Test
	fun shouldConsumeKeyEventForName() {
		val switchView = SwitchView()
		val keyEvent = keyEvent('A'.toInt())
		val context = contextFor(keyEvent = keyEvent)
		switchView.name = "A"
		switchView.getActorInteractionHandler(context).keyPressed(context)

		verify { keyEvent.consume() }
	}

	@Test
	fun shouldNotConsumeKeyEventForOtherName() {
		val switchView = SwitchView()
		val keyEvent = keyEvent('B'.toInt())
		val context = contextFor(keyEvent = keyEvent)
		switchView.name = "A"
		switchView.getActorInteractionHandler(context).keyPressed(context)

		verify(exactly = 0) { keyEvent.consume() }
	}

	@Test
	fun shouldToggleWithMousePress() {
		val switchView = SwitchView()
		switchView.toggle = false
		val event = MouseEventImpl(type = MouseEventType.PRESSED, button = Button.BUTTON1)
		val context = contextFor(mouseEvent = event)
		switchView.getActorInteractionHandler(context).mousePressed(context)

		assertTrue(switchView.model.isOn)
	}

	@Test
	fun shouldNotToggleWithReleaseWithoutPrecedingPress() {
		val switchView = SwitchView()
		switchView.toggle = false
		val event = MouseEventImpl(
			type = MouseEventType.RELEASED,
			button = Button.BUTTON1)
		val context = contextFor(mouseEvent = event)
		switchView.getActorInteractionHandler(context).mouseReleased(context)

		assertFalse(switchView.model.isOn)
	}

	@Test
	fun shouldNotToggleWithRightMouseButton() {
		val switchView = SwitchView()
		switchView.toggle = false
		val event = MouseEventImpl(
			type = MouseEventType.PRESSED,
			button = Button.BUTTON3)
		val context = contextFor(mouseEvent = event)
		switchView.getActorInteractionHandler(context).mousePressed(context)

		assertFalse(switchView.model.isOn)
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
			view = mockk(relaxed = true),
			mouseEvent = mouseEvent,
			keyEvent = keyEvent,
			x = 0.0,
			y = 0.0)
	}
}