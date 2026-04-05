package io.antarescircuit.jabbah.graph.ui.knob

import io.antarescircuit.jabbah.base.event.Button
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
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
        knobView.getActorInteractionHandler(context).mouseClicked(context)
    }

    private fun contextFor(x: Double, y: Double, clickCount: Int = 0): ActorInteractionContext {
        val mouseEvent = mock<MouseEvent>()
        every { mouseEvent.clickCount } returns clickCount
        every { mouseEvent.button} returns Button.BUTTON1
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