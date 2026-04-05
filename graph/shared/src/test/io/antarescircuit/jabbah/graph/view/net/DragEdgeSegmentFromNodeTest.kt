package io.antarescircuit.jabbah.graph.view.net

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.AbstractGraphViewEditingTest
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class DragEdgeSegmentFromNodeTest : AbstractGraphViewEditingTest() {

    override fun setupCircuit() {
        val v1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v1", 100, 100))
        val v2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v2", 200, 100))
        val v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v2", 200, 200))
        val ev = builder.connect(v1, v2)
        builder.split(ev, 0, Point2D(150, 100), v3.getPortView(v3.model.getPort()))
    }

    @Test
    fun shouldNotCreateMoveComponentCommand() {
        val ev = builder.graphView.getEdgeViews().first { it.id == 7 }.polyline

        assertEquals(3, builder.graphView.getEdgeViews().size)
        assertEquals(Point2D(150, 100), ev.getPointAt(0))

        // Do a segment move of the new EdgeView
        driver.mouseMoveTo(150, 150)
        driver.pressMouseAt(150, 150)
        driver.dragMouseTo(170, 150)
        driver.releaseMouseAt(170, 150)

        assertEquals(Point2D(170, 100), ev.getPointAt(0))
        assertEquals(Point2D(170, 200), ev.getPointAt(1))
        assertEquals(Point2D(200, 200), ev.getPointAt(2))
        assertEquals(1, editor.commandManager.commandCount)
    }
}