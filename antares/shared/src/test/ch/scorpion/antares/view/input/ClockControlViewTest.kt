package ch.scorpion.antares.view.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.container.OriginIndicator
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.ui.KnobLauncher
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

class ClockControlViewTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldFindRotatedIconButton() {
		val knobLauncher = mockk<KnobLauncher>(relaxed = true)
		val controlViewSource = ClockView()

		val containerDrawing = ContainerDrawing()
		containerDrawing.add(ControlViewComponent(source = controlViewSource as ControlViewSource<Vertice>))
		containerDrawing.add(OriginIndicator())

		val vv = SubGraphVerticeViewImpl()
		vv.fillFromContainerDrawing(containerDrawing)
		val controlView = vv.getControlViewComponents().first().controlView as ClockControlView
		controlView.knobLauncher = knobLauncher
		controlView.bindControlView(vv, controlViewSource.model)
		vv.rotate(RotationDirection.CounterClockwise)

		val context = contextFor(ClockControlView.ICON_BUTTON_SIZE / 2.0, -ClockControlView.ICON_BUTTON_SIZE / 2.0)
		vv.getActorInteractionHandler(context).mouseMoved(context)

		verify { knobLauncher.launchAfterDelay(any(), any(), any(), any(), any(), any()) }
	}

	private fun contextFor(x: Double, y: Double, clickCount: Int = 0): ActorInteractionContext {
		val mouseEvent = mockk<MouseEvent>()
		every { mouseEvent.clickCount } returns clickCount
		every { mouseEvent.button} returns Button.BUTTON1
		return ActorInteractionContext(
			signalHandler = mockk(),
			view = mockk(),
			mouseEvent = mouseEvent,
			keyEvent = mockk(),
			x = x,
			y = y
		)
	}
}