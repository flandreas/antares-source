package ch.scorpion.antares.view.input

import ch.scorpion.jabbah.base.math.PI_2
import ch.scorpion.jabbah.base.math.TWO_PI
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.graph.ui.KnobModel
import ch.scorpion.jabbah.graph.ui.KnobView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import io.mockk.every
import io.mockk.mockk
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [KnobModel]. */
class KnobModelTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@Test
	fun shouldCalculateAngle() {
		assertEquals(0.0, KnobModel(0).asAngle)
		assertEquals(0.0, KnobModel(1).asAngle)
		assertEquals(0.0, KnobModel(10).asAngle)

		assertEquals(0.0, KnobModel(100).asAngle)
		assertEquals(PI, KnobModel(550).asAngle)
	}

	@Test
	fun shouldChangeToAngle() {
		assertEquals(2L, KnobModel(1).incrementAngleTo(TWO_PI / 9))
		assertEquals(6L, KnobModel(1).incrementAngleTo(5 * TWO_PI / 9))

		assertEquals(20_000L, KnobModel(10_000).incrementAngleTo(TWO_PI / 9))
		assertEquals(60_000L, KnobModel(10_000).incrementAngleTo(5 * TWO_PI / 9))
	}

	@Test
	fun shouldIncrementAngleAcrossOrigin() {
		assertEquals(3_250L, KnobModel(999).incrementAngleTo(PI_2))
	}
}

class KnobViewTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@Test
	fun shouldSingleDragQuarterClockwise() {
		val view = KnobView(KnobModel(100))
		pressMouseAt(view, 0.0, -200.0)

		dragMouseTo(view, 200.0, 0.0)

		assertEquals(100L + 900 / 4, view.value)
	}

	@Test
	fun shouldIncrementalDragQuarterClockwise() {
		val view = KnobView(KnobModel(100))
		pressMouseAt(view, 0.0, -200.0)

		dragMouseTo(view, 200.0, -200.0)
		dragMouseTo(view, 200.0, 0.0)

		assertEquals(100L + 900 / 4, view.value)
	}

	@Test
	fun shouldStartDragAnywhere() {
		val view = KnobView(KnobModel(100))
		pressMouseAt(view, 200.0, 0.0)

		dragMouseTo(view, 0.0, 200.0)

		assertEquals(100L + 900 / 4, view.value)
	}

	@Test
	fun shouldApplyDefaultValueOnDoubleClick() {
		val view = KnobView(KnobModel(100))
		view.value = 1_000

		doubleClickAt(view, 0.0, 0.0)

		assertEquals(100, view.value)
	}

	private fun pressMouseAt(knobView: KnobView, x: Double, y: Double) {
		val context = contextFor(x, y)
		knobView.getActorInteractionHandler(context).mousePressed(context)
	}

	private fun dragMouseTo(knobView: KnobView, x: Double, y: Double) {
		val context = contextFor(x, y)
		knobView.getActorInteractionHandler(context).mouseDragged(context)
	}

	private fun doubleClickAt(knobView: KnobView, x: Double, y: Double) {
		val context = contextFor(x, y, clickCount = 2)
		knobView.getActorInteractionHandler(context).mousePressed(context)
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