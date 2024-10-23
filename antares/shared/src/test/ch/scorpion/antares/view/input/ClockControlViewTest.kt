package ch.scorpion.antares.view.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.container.OriginIndicator
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.ImmediateVerticeLink
import ch.scorpion.jabbah.graph.ui.KnobLauncher
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.Test

class ClockControlViewTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldFindRotatedIconButton() {
		val knobLauncher = mock<KnobLauncher>(MockMode.autofill)
		val controlViewSource = ClockView()

		val containerDrawing = ContainerDrawing()
		containerDrawing.add(ControlViewComponent(source = controlViewSource as ControlViewSource<Vertice>))
		containerDrawing.add(OriginIndicator())

		val graph = mock<Graph>(MockMode.autofill)
		every { graph.withId(any()) } returns controlViewSource.model

		val vv = SubGraphVerticeViewImpl()
		vv.fillFromContainerDrawing(containerDrawing)
		val controlView = vv.getControlViewComponents().first().controlView as ClockControlView
		controlView.knobLauncher = knobLauncher
		controlView.bindControlView(vv, ImmediateVerticeLink(controlViewSource.model.id), graph)
		vv.rotate(RotationDirection.CounterClockwise)

		val context = contextFor(ClockControlView.ICON_BUTTON_SIZE / 2.0, -ClockControlView.ICON_BUTTON_SIZE / 2.0)
		vv.getActorInteractionHandler(context).mouseMoved(context)

		verify { knobLauncher.launchAfterDelay(any(), any(), any(), any(), any(), any(), any()) }
	}

	private fun contextFor(x: Double, y: Double, clickCount: Int = 0): ActorInteractionContext {
		val mouseEvent = mock<MouseEvent>()
		every { mouseEvent.clickCount } returns clickCount
		every { mouseEvent.button} returns Button.BUTTON1
		return ActorInteractionContext(
			signalHandler = mock(),
			view = mock(MockMode.autofill),
			mouseEvent = mouseEvent,
			keyEvent = mock(),
			x = x,
			y = y
		)
	}
}