package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.connect.SplitEdgeViewResult
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame

class GraphViewServiceImplTest {

    companion object {
        init {
            GraphViewTestRule.configure()
        }
    }

    private val service = GraphViewServiceImpl()

    private val builder = GraphViewBuilder<Boolean>()
    private val drawingView: DrawingView<Drawing<Component>> = DrawingViewMockBuilder().withDrawing(builder.graphView).build()
    private val vv1 = builder.addVerticeView(TestVerticeView("vv1", loc = Point2D(100, 100)))
    private val vv2 = builder.addVerticeView(TestVerticeView("vv2", loc = Point2D(200, 100)))
    private val vv3 = builder.addVerticeView(TestVerticeView("vv3", loc = Point2D(200, 200)))
    private lateinit var ev: EdgeView<Boolean>
    private lateinit var split: SplitEdgeViewResult<Boolean>

    @Test
    fun shouldDeleteConnectedVerticeView() {
        val ev = builder.connect(vv1, vv2)

        service.delete(listOf(vv1), drawingView.drawing)

        assertFalse(drawingView.drawing.contains(vv1))
        assertFalse((drawingView.drawing as GraphView).graph!!.contains(vv1.model))

        assertNull(ev.origin)
        assertNull(vv1.model.getOutput<Boolean>().net)
        assertSame(vv2, ev.destination!!.connectableView)
        assertSame(ev.model, vv2.model.getInput<Boolean>().net)
    }

    @Test
    fun shouldDeleteConnectedEdgeView() {
        val ev = builder.connect(vv1, vv2)

        service.delete(listOf(ev), drawingView.drawing)

        assertNull(vv1.model.getOutput<Boolean>().net)
        assertNull(vv2.model.getInput<Boolean>().net)
        assertEquals(3, (drawingView.drawing as GraphView).graph!!.elementsCount)
        assertEquals(0, (drawingView.drawing as GraphView).graph!!.elements.filter { it is Net<*> }.size)
    }
}