package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.assertEquals

abstract class AbstractForkEdgeViewTest {

    protected val builder: GraphViewBuilder<Boolean>
    protected val drawingViewBuilder: DrawingViewMockBuilder
    protected val v1: TestVerticeView
    protected val v2: TestVerticeView
    protected val v3: TestVerticeView
    protected val v4: TestVerticeView
    protected val v5: TestVerticeView

    protected lateinit var ev1: EdgeView<Boolean>
    protected lateinit var ev2: EdgeView<Boolean>
    protected lateinit var ev3: EdgeView<Boolean>
    protected lateinit var ev4: EdgeView<Boolean>

    init {
        GraphViewTestRule.configure()
        builder = GraphViewBuilder<Boolean>()
        drawingViewBuilder = DrawingViewMockBuilder().withDrawing(builder.build())
        v1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v1", 100, 100))
        v2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v2", 200, 100))
        v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
        v4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 200, 300))
        v5 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v5", 200, 300))

        EditModule.commandManager.bindDataHolder(builder)
        fillGraphView()
    }

    private fun fillGraphView() {
        builder.addVerticeView(v1)
        builder.addVerticeView(v2)
        builder.addVerticeView(v3)
        builder.addVerticeView(v4)
        builder.addVerticeView(v5)

        ev1 = builder.connect(v1, v2)
        ev2 = builder.split(ev1, 0, Point2D(150, 100), v3).newEdgeView
        ev3 = builder.split(ev2, 0, Point2D(150, 200), v4).newEdgeView
        ev4 = builder.split(ev3, 0, Point2D(150, 300), v5).newEdgeView

        assertEquals(3, builder.graphView.getNodeViews().size)
        assertEquals(7, builder.graphView.getEdgeViews().size)
    }
}