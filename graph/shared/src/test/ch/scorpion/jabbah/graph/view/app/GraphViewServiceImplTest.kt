package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.DrawingViewMockBuilder
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** Unit tests for [GraphViewServiceImpl].*/
class GraphViewServiceImplTest {

    companion object {
	    init {
	    	GraphViewTestRule.configure()
	    }
    }

    private val connectService = GraphViewModule.graphViewConnectService
    private val service = GraphViewServiceImpl()
    private lateinit var testGraphView: TestGraphView
    private lateinit var vv3: TestVerticeView

    @BeforeTest
    fun setup() {
        testGraphView = TestGraphView()
        vv3 = TestVerticeView(name = "3", loc = Point2D(200, 200))
    }

    @Test
    fun shouldUndoDeleteAfterNode() {
        val newEdgeView = setupSplittedEdgeViews()

        service.delete(listOf(newEdgeView), DrawingViewMockBuilder().withDrawing(testGraphView.graphView).build())
        assertEquals(4, testGraphView.graphView.drawablesCount)

        EditModule.commandManager.undo()

	    assertEquals(3, (newEdgeView.origin as NodeView<Boolean>).getEdgeViews().size)
	    assertEquals(3, getNodeView().getEdgeViews().size)
        assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.origin == testGraphView.vv1 })
        assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.destination == testGraphView.vv2 })
        assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.destination == vv3 })
    }

    @Test
    fun shouldUndoDeleteBeforeNode() {
        setupSplittedEdgeViews()

        service.delete(listOf(testGraphView.ev), DrawingViewMockBuilder().withDrawing(testGraphView.graphView).build())
        assertEquals(4, testGraphView.graphView.drawablesCount)

        EditModule.commandManager.undo()

	    assertEquals(3, getNodeView().getEdgeViews().size)
	    assertEquals(Point2D(150, 100), getNodeView().location)
        assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.origin == testGraphView.vv1 })
        assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.destination == testGraphView.vv2 })
        assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.destination == vv3 })
    }

	@Test
	fun shouldUndoDeleteAll() {
		setupSplittedEdgeViews()
		service.delete(testGraphView.graphView.getDrawables().toList(), DrawingViewMockBuilder().withDrawing(testGraphView.graphView).build())

		EditModule.commandManager.undo()

		assertEquals(3, getNodeView().getEdgeViews().size)
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