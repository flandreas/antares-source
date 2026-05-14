package io.antarescircuit.jabbah.graph.ui.knob

import io.antarescircuit.jabbah.base.event.Button
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude.One
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.Factor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KnobViewTest {

    @BeforeTest
    fun setup() {
        GraphViewTestRule.configure()
    }

    @Test
    fun shouldSingleDragQuarterClockwise() {
        val view = KnobView(KnobModel(MagnitudeValue(100, One, Factor)))
        pressMouseAt(view, 0.0, -200.0)

        dragMouseTo(view, 200.0, 0.0)

        assertEquals(MagnitudeValue(100L + 900 / 4, One, Factor), view.value)
    }

    @Test
    fun shouldIncrementalDragQuarterClockwise() {
        val view = KnobView(KnobModel(MagnitudeValue(100, One, Factor)))
        pressMouseAt(view, 0.0, -200.0)

        dragMouseTo(view, 200.0, -200.0)
        dragMouseTo(view, 200.0, 0.0)

        assertEquals(MagnitudeValue(100L + 900 / 4, One, Factor), view.value)
    }

    @Test
    fun shouldApplyDefaultValueOnDoubleClick() {
        val view = KnobView(KnobModel(MagnitudeValue(100, One, Factor)))
        view.value = MagnitudeValue(900, One, Factor)

        doubleClickAt(view, 0.0, 0.0)

        assertEquals(MagnitudeValue(100, One, Factor), view.value)
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
        knobView.getActorInteractionHandler(context).mouseClicked(context)
    }

    private fun contextFor(x: Double, y: Double, clickCount: Int = 0, altDown: Boolean = false): ActorInteractionContext {
        val mouseEvent = mock<MouseEvent>()
        every { mouseEvent.clickCount } returns clickCount
        every { mouseEvent.button} returns Button.BUTTON1
        every { mouseEvent.isAltDown } returns altDown
        return ActorInteractionContext(
            signalHandler = mock(),
            view = mock(),
            mouseEvent = mouseEvent,
            keyEvent = mock(),
            x = x,
            y = y
        )
    }
}