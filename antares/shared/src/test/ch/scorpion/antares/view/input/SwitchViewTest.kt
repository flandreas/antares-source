package ch.scorpion.antares.view.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionContextImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

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
		val context = contextFor(keyEvent)
		switchView.name = "A"
		switchView.getActorInteractionHandler(context)?.keyPressed(context)

		verify { keyEvent.consume() }
	}

	@Test
	fun shouldNotCOnsumeKeyEventForOtherName() {
		val switchView = SwitchView()
		val keyEvent = keyEvent('B'.toInt())
		val context = contextFor(keyEvent)
		switchView.name = "A"
		switchView.getActorInteractionHandler(context)?.keyPressed(context)

		verify(exactly = 0) { keyEvent.consume() }
	}

	private fun keyEvent(key: Int): KeyEvent {
		val keyEvent: KeyEvent = mockk(relaxed = true)
		every { keyEvent.key } returns key
		every { keyEvent.modifiers } returns 0
		return keyEvent
	}

	private fun contextFor(keyEvent: KeyEvent): ActorInteractionContext {
		return ActorInteractionContextImpl(
			signalHandler = mockk(relaxed = true),
			view = mockk(relaxed = true),
			mouseEvent = mockk(),
			keyEvent = keyEvent,
			x = 0.0,
			y = 0.0)
	}
}