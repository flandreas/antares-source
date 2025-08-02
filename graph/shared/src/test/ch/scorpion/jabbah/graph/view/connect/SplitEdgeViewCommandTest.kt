package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.GraphEditorMockBuilder
import ch.scorpion.jabbah.graph.health.GraphViewConsistencyCheck
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class SplitEdgeViewCommandTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val service = GraphViewModule.graphViewConnectService
	private val edgeViewFactory = GraphViewModule.getEdgeViewFactory()
	private val editorBuilder = GraphEditorMockBuilder()
	private lateinit var testGraphView: TestGraphView

	@BeforeTest
	fun setup() {
		Translations.withAnyKey()
		testGraphView = TestGraphView()
		editorBuilder.withDrawing(testGraphView.graphView)
	}

	@Test
	fun shouldSplitEdgeView() {
		val newEdgeView = edgeViewFactory.createEdgeView(testGraphView.graphView, testGraphView.netView)
		newEdgeView.addSegmentPoint(Point2D(150, 100))
		editorBuilder.editor.drawing.add(newEdgeView)

		val command = SplitEdgeViewCommand(
			editor = editorBuilder.editor,
			connectService = service,
			splitEdgeViewId = testGraphView.ev.id,
			splitLocation = EdgeViewEndpointType.ORIGIN.getLocation(newEdgeView),
			segmentIndex = 0,
			newEdgeViewProvider = NewEdgeViewAtSplitCloneProvider(newEdgeView),
			newEdgeViewEndpointType = EdgeViewEndpointType.ORIGIN,
			targetConnectableViewId = null,
			targetPortId = null
		)
		command.execute()

		// Model assertions: 2 Vertices, 1 Net
		assertEquals(3, testGraphView.graph.elementsCount)
		assertSame(testGraphView.net, testGraphView.v1.getOutput<Boolean>().net)

		// View assertions
		val nodeView = command.result.newEdgeView.origin?.connectableView as NodeView<Boolean>
		assertSame(testGraphView.vv1, testGraphView.ev.origin?.connectableView as TestVerticeView)
		assertSame(nodeView, testGraphView.ev.destination?.connectableView as NodeView<Boolean>)
		assertEquals(nodeView.location, testGraphView.ev.getSegmentPoint(testGraphView.ev.segmentPointCount - 1))

		val ev = nodeView.getOutgoingEdgeViews()[1]
		assertSame(nodeView, ev.origin?.connectableView as NodeView<Boolean>)
		assertSame(testGraphView.vv2, ev.destination?.connectableView as TestVerticeView)
		assertEquals(nodeView.location, ev.getSegmentPoint(0))

		GraphViewConsistencyCheck.execute(testGraphView.graphView)
	}
}