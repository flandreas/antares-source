package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.edit.editor.TestEditorBuilder
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.sameInstance
import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [SplitEdgeViewCommand].
 */
class SplitEdgeViewCommandTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    private val service = GraphViewModule.graphViewConnectService
    private val edgeViewFactory = GraphViewModule.getEdgeViewFactory<Boolean>()
    private lateinit var testGraphView: TestGraphView

    @Before
    fun setup() {
        TestTranslationsBuilder().withAnyKey()
        testGraphView = TestGraphView()
    }

    @Test
    fun shouldSplitEdgeView() {
        val editorBuilder = TestEditorBuilder()
        val newEdgeView = edgeViewFactory.createEdgeView(testGraphView.net)
        newEdgeView.addSegmentPoint(Point2D(150, 100))

        val command = SplitEdgeViewCommand(
                editor = editorBuilder.editor,
                connectService = service,
                graphView = testGraphView.graphView,
                origEdgeView = testGraphView.ev,
                segmentIndex = 0,
                newEdgeView = newEdgeView,
                targetPortView = null,
                nodeView = null
        )
        command.execute()

        // Model assertions: 2 Vertices, 1 Net
        assertThat(testGraphView.graph.elementsCount, `is`(3))
        assertThat(testGraphView.v1.getOutput<Boolean>().net, `is`(sameInstance(testGraphView.net)))

        // View assertions
        val nodeView = newEdgeView.origin as NodeView<Boolean>
        assertThat(testGraphView.ev.origin as TestVerticeView, `is`(sameInstance(testGraphView.vv1)))
        assertThat(testGraphView.ev.destination as NodeView<Boolean>, `is`(sameInstance(nodeView)))
        assertThat(testGraphView.ev.getSegmentPoint(testGraphView.ev.segmentPointCount - 1), `is`(nodeView.location))

        val ev = nodeView.getOutgoingEdgeViews()[1]
        assertThat(ev.origin as NodeView<Boolean>, `is`(sameInstance(nodeView)))
        assertThat(ev.destination as TestVerticeView, `is`(sameInstance(testGraphView.vv2)))
        assertThat(ev.getSegmentPoint(0), `is`(nodeView.location))
    }
}