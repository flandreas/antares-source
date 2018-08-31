package ch.scorpion.antares.view.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.MathClass
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionContextImpl
import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [KnobModel]. */
class KnobModelTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = AntaresTestRule()
	}

	@Test
	fun shouldCalculateAngle() {
		assertThat(KnobModel(0).asAngle, `is`(0.0))
		assertThat(KnobModel(1).asAngle, `is`(0.0))
		assertThat(KnobModel(10).asAngle, `is`(0.0))

		assertThat(KnobModel(100).asAngle, `is`(0.0))
		assertThat(KnobModel(550).asAngle, `is`(MathClass.PI))
	}

	@Test
	fun shouldChangeToAngle() {
		assertThat(KnobModel(1).incrementAngleTo(MathClass.TWO_PI / 9), `is`(2L))
		assertThat(KnobModel(1).incrementAngleTo(5 * MathClass.TWO_PI / 9), `is`(6L))

		assertThat(KnobModel(10_000).incrementAngleTo(MathClass.TWO_PI / 9), `is`(20_000L))
		assertThat(KnobModel(10_000).incrementAngleTo(5 * MathClass.TWO_PI / 9), `is`(60_000L))
	}

	@Test
	fun shouldIncrementAngleAcrossOrigin() {
		assertThat(KnobModel(999).incrementAngleTo(MathClass.PI_2), `is`(3_250L))
	}
}

class KnobViewTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = AntaresTestRule()
	}

	@Test
	fun shouldSingleDragQuarterClockwise() {
		val model = KnobModel(100)
		val view = KnobView(model = model)
		pressMouseAt(view, 0.0, -200.0)

		dragMouseTo(view, 200.0, 0.0)

		assertThat(view.value, `is`(100L + 900 / 4))
	}

	@Test
	fun shouldIncrementalDragQuarterClockwise() {
		val model = KnobModel(100)
		val view = KnobView(model = model)
		pressMouseAt(view, 0.0, -200.0)

		dragMouseTo(view, 200.0, -200.0)
		dragMouseTo(view, 200.0, 0.0)

		assertThat(view.value, `is`(100L + 900 / 4))
	}

	@Test
	fun shouldStartDragAnywhere() {
		val model = KnobModel(100)
		val view = KnobView(model = model)
		pressMouseAt(view, 200.0, 0.0)

		dragMouseTo(view, 0.0, 200.0)

		assertThat(view.value, `is`(100L + 900 / 4))
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
			signalHandler = mock(),
			view = mock(),
			mouseEvent = mock(),
			keyEvent = mock(),
			x = x,
			y = y)
	}
}