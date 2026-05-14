package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.base.event.Button
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.draw.drawable.RotationDirection
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.container.ControlViewComponent
import io.antarescircuit.jabbah.graph.container.OriginIndicator
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.vertice.ImmediateVerticeLink
import io.antarescircuit.jabbah.graph.ui.knob.KnobLauncher
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.Test

class ClockControlViewTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldFindRotatedIconButton() {
		val knobLauncher = mock<KnobLauncher>(MockMode.autofill)
		val controlViewSource = ClockView()

		val containerDrawing = ContainerDrawing()
		@Suppress("UNCHECKED_CAST")
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

		verify { knobLauncher.launchAfterDelay(any(), any(), any(), any(), any(), any()) }
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