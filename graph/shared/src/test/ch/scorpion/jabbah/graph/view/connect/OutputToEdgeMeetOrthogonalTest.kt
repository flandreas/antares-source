package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OutputToEdgeMeetOrthogonalTest : AbstractInputEventHandlerTest() {

    init {
        handler = GraphViewModule.outputToInputOrEdgeConnector.handler
    }

    @Test
    fun shouldAdjustToMeetEdgeViewOrthogonally() {
        CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints

        // Create target EdgeView to v2 at x=150
        builder.connectInputOpen(v2, Point2D(150, 200))
        assertNotNull(builder.graphView.getEdgeViews().first())

        mouseMoveToAndClickAt(130, 100)
        mouseMoveTo(150, 150)

        val ev = builder.graphView.getEdgeViews().first()
        assertEquals(4, ev.segmentPointCount)
        assertEquals(Direction.EAST, ev.getSegmentDirection(2))
    }

    @Test
    fun shouldDragToMeetEdgeViewOrthogonally() {
        CurrentConnectMethod.defaultMethod = ConnectMethod.AutoLayout

        // Create target EdgeView to v2 at x=150
        builder.connectInputOpen(v2, Point2D(150, 200))
        assertNotNull(builder.graphView.getEdgeViews().first())

        mouseMoveTo(130, 100)
        pressMouseAt(130, 100)
        dragMouseTo(150, 150)

        val ev = builder.graphView.getEdgeViews().first()
        assertEquals(4, ev.segmentPointCount)
        assertEquals(Direction.EAST, ev.getSegmentDirection(2))
    }
}