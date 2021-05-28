package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.container.OriginIndicator
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

class SubGraphVerticeViewImplTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@Test
	fun shouldFindRotatedControlView() {
		val handler = mockk<ActorInteractionHandler>(relaxed = true)
		val controlView = TestControlVerticeView(rectangle = Rectangle2D(0, 0, 100, 100))

		val containerDrawing = ContainerDrawing()
		containerDrawing.add(ControlViewComponent(source = controlView as ControlViewSource<Vertice>))
		containerDrawing.add(OriginIndicator())

		// This clones the ControlViews, so set up handler on clone
		val vv = SubGraphVerticeViewImpl()
		vv.fillFromContainerDrawing(containerDrawing)
		(vv.getControlViewComponents().first().controlView as TestControlVerticeView).actorInteractionHandler = handler
		vv.rotateCounterClockwise()

		val context = contextFor(50.0, -50.0)
		vv.getActorInteractionHandler(context).mouseMoved(context)

		verify { handler.mouseMoved(any()) }
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