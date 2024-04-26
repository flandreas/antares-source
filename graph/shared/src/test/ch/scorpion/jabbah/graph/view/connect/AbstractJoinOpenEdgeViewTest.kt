package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.health.GraphViewConsistencyCheck
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.assertEquals
import kotlin.test.assertSame

abstract class AbstractJoinOpenEdgeViewTest(
    handler: InputEventHandler<EditInputEventContext>
) : AbstractInputEventHandlerTest(handler) {

    companion object {
        init {
            GraphViewTestRule.configure()
        }
    }

    protected val v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))

    init {
        builder.connectOutputOpen(v1, Point2D(150, 100))
        builder.connectInputOpen(v3, Point2D(150, 200))
        editor.commandManager.reset()
    }

    protected fun assertJoined() {
        val effV1 = builder.graphView.getWithId(1) as TestVerticeView
        val effV3 = builder.graphView.getWithId(3) as TestVerticeView
        val edgeViews = builder.graphView.getEdgeViews()

        assertEquals(1, edgeViews.size)

        // Check model consistency
        assertEquals(1, builder.graph.elements.count { it is Net<*> })
        assertSame(edgeViews[0].net, effV1.model.getOutput<Boolean>().net)
        assertSame(edgeViews[0].net, effV3.model.getInput<Boolean>().net)

        // Check view geometry
        assertEquals(Point2D(120, 100), edgeViews[0].polyline.getPointAt(0))
        assertEquals(Point2D(150, 100), edgeViews[0].polyline.getPointAt(1))
        assertEquals(Point2D(150, 200), edgeViews[0].polyline.getPointAt(2))
        assertEquals(Point2D(200, 200), edgeViews[0].polyline.getPointAt(3))

        GraphViewConsistencyCheck.execute(builder.graphView)
    }
}