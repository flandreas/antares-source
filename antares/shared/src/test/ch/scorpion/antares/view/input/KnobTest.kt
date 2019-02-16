package ch.scorpion.antares.view.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.PI_2
import ch.scorpion.jabbah.base.TWO_PI
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionContextImpl
import io.mockk.mockk
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [KnobModel]. */
class KnobModelTest {

	companion object {
		init {
			AntaresTestRule.configure()
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
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldSingleDragQuarterClockwise() {
		val model = KnobModel(100)
		val view = KnobView(model = model)
		pressMouseAt(view, 0.0, -200.0)

		dragMouseTo(view, 200.0, 0.0)

		assertEquals(100L + 900 / 4, view.value)
	}

	@Test
	fun shouldIncrementalDragQuarterClockwise() {
		val model = KnobModel(100)
		val view = KnobView(model = model)
		pressMouseAt(view, 0.0, -200.0)

		dragMouseTo(view, 200.0, -200.0)
		dragMouseTo(view, 200.0, 0.0)

		assertEquals(100L + 900 / 4, view.value)
	}

	@Test
	fun shouldStartDragAnywhere() {
		val model = KnobModel(100)
		val view = KnobView(model = model)
		pressMouseAt(view, 200.0, 0.0)

		dragMouseTo(view, 0.0, 200.0)

		assertEquals(100L + 900 / 4, view.value)
	}

	private fun pressMouseAt(knobView: KnobView, x: Double, y: Double) {
		val context = contextFor(x, y)
		knobView.getActorInteractionHandler(context)?.mousePressed(context)
	}

	private fun dragMouseTo(knobView: KnobView, x: Double, y: Double) {
		val context = contextFor(x, y)
		knobView.getActorInteractionHandler(context)?.mouseDragged(context)
	}

	private fun contextFor(x: Double, y: Double): ActorInteractionContext {
		return ActorInteractionContextImpl(
			signalHandler = mockk(),
			view = mockk(),
			mouseEvent = mockk(),
			keyEvent = mockk(),
			x = x,
			y = y)
	}
}