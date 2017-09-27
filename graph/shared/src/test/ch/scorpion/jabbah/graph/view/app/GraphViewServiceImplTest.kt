package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingViewMockBuilder
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [GraphViewServiceImpl].*/
class GraphViewServiceImplTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    private val connectService = GraphViewModule.graphViewConnectService
    private val service = GraphViewServiceImpl()
    private lateinit var testGraphView: TestGraphView

    @Before
    fun setup() {
        TestTranslationsBuilder().withAnyKey()
        testGraphView = TestGraphView()
    }

    @Test
    fun shouldUndoDelete() {
        val vv3 = TestVerticeView()
        vv3.location = Point2D(200, 200)
        testGraphView.graphView.add(vv3)

        val newEdgeView = GraphViewModule.getEdgeViewFactory<Boolean>().createEdgeView(testGraphView.net)
        newEdgeView.addSegmentPoint(Point2D(150, 100))
        newEdgeView.addSegmentPoint(Point2D(150, 200))
        newEdgeView.addSegmentPoint(Point2D(200, 200))
        connectService.split(
                testGraphView.graphView,
                testGraphView.ev,
                0,
                newEdgeView,
                vv3.getPortView(vv3.model!!.getPort())
        )

        service.delete(listOf(newEdgeView), DrawingViewMockBuilder().withDrawing(testGraphView.graphView).build<Component>())
        assertThat(testGraphView.graphView.drawablesCount, `is`(4))

        EditModule.commandManager.undo()

        assertThat((newEdgeView.origin as NodeView<Boolean>).getEdgeViews().size, `is`(3))
        assertThat(testGraphView.graphView.getDrawables { it is NodeView<*> }.map { it as NodeView<*> }.first().getEdgeViews().size, `is`(3));
    }
}