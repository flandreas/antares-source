package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingViewMockBuilder
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
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
    private lateinit var vv3: TestVerticeView

    @Before
    fun setup() {
        TestTranslationsBuilder().withAnyKey()
        testGraphView = TestGraphView()
        vv3 = TestVerticeView(name = "3", loc = Point2D(200, 200))
    }

    @Test
    fun shouldUndoDeleteAfterNode() {
        val newEdgeView = setupSplittedEdgeViews()

        service.delete(listOf(newEdgeView), DrawingViewMockBuilder().withDrawing(testGraphView.graphView).build<Component>())
        assertThat(testGraphView.graphView.drawablesCount, `is`(4))

        EditModule.commandManager.undo()

        assertThat((newEdgeView.origin as NodeView<Boolean>).getEdgeViews().size, `is`(3))
        assertThat(getNodeView().getEdgeViews().size, `is`(3))
        assertThat(getNodeView().getEdgeViews().firstOrNull { it.origin == testGraphView.vv1 }, `is`(notNullValue()))
        assertThat(getNodeView().getEdgeViews().firstOrNull { it.destination == testGraphView.vv2 }, `is`(notNullValue()))
        assertThat(getNodeView().getEdgeViews().firstOrNull { it.destination == vv3 }, `is`(notNullValue()))
    }

    @Test
    fun shouldUndoDeleteBeforeNode() {
        setupSplittedEdgeViews()

        service.delete(listOf(testGraphView.ev), DrawingViewMockBuilder().withDrawing(testGraphView.graphView).build<Component>())
        assertThat(testGraphView.graphView.drawablesCount, `is`(4))

        EditModule.commandManager.undo()

        assertThat(getNodeView().getEdgeViews().size, `is`(3))
        assertThat(getNodeView().location, `is`(Point2D(150, 100)))
        assertThat(getNodeView().getEdgeViews().firstOrNull { it.origin == testGraphView.vv1 }, `is`(notNullValue()))
        assertThat(getNodeView().getEdgeViews().firstOrNull { it.destination == testGraphView.vv2 }, `is`(notNullValue()))
        assertThat(getNodeView().getEdgeViews().firstOrNull { it.destination == vv3 }, `is`(notNullValue()))
    }

    private fun setupSplittedEdgeViews(): EdgeView<Boolean> {
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
        return newEdgeView
    }

    private fun getNodeView(): NodeView<*> {
        return testGraphView.graphView.getDrawables { it is NodeView<*> }.map { it as NodeView<*> }.first()
    }
}