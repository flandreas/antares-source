package io.antarescircuit.jabbah.graph.view.graph

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.graph.GraphStorable
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import io.antarescircuit.jabbah.io.StorableCloner
import org.junit.Test
import kotlin.test.assertEquals

import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.connect.SplitEdgeViewResult
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for copy/paste of selections containing various combinations of [EdgeView]s and [NodeView]s.
 */
class GraphViewCopyPasteServiceImplNodeTest {

	private val service: GraphViewCopyPasteService
	private val builder: GraphViewBuilder<Boolean>
	private val drawingViewBuilder: DrawingViewMockBuilder
	private val vv1: TestVerticeView
	private val vv2: TestVerticeView
	private val vv3: TestVerticeView
	private val vv4: TestVerticeView
	private val ev12: EdgeView<Boolean>
	private val split3: SplitEdgeViewResult<Boolean>
	private val split4: SplitEdgeViewResult<Boolean>

	init {
		GraphViewTestRule.configure()

		service = GraphViewCopyPasteService()
		builder = GraphViewBuilder()
		drawingViewBuilder = DrawingViewMockBuilder().withDrawing(builder.build())
		vv1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv1", 100, 100))
		vv2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv2", 200, 100))
		vv3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv3", 200, 200))
		vv4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv4", 200, 300))
		ev12 = builder.connect(vv1, vv2)
		split3 = builder.split(ev12, 0, Point2D(150, 100), vv3)
		split4 = builder.split(split3.newEdgeView, 0, Point2D(150, 200), vv4)
	}

	@Test
	fun shouldNotPasteNodeViewAtOneEdgeView() {
		service.paste(
			service.copy(listOf(split3.nodeView.id, split3.tailEdgeView.id), builder.graphView),
			drawingViewBuilder.build()
		)

		assertEquals(11 + 1, builder.graphView.drawables.size)
		assertEquals(2, builder.graphView.getDrawables { it is NodeView<*> }.size)
		checkReadWrite()
	}

	@Test
	fun shouldNotPasteNodeViewAtTwoEdgeViews() {
		service.paste(
			service.copy(listOf(split3.nodeView.id, split3.tailEdgeView.id, split3.newEdgeView.id), builder.graphView),
			drawingViewBuilder.build()
		)

		assertEquals(11 + 1, builder.graphView.drawables.size)
		assertEquals(2, builder.graphView.getDrawables { it is NodeView<*> }.size)
		checkReadWrite()
	}

	@Test
	fun shouldNotPasteTwoNodeViewAtTwoEdgeViews() {
		val result = service.paste(
			service.copy(listOf(split3.nodeView.id, split3.tailEdgeView.id, split3.newEdgeView.id, split4.nodeView.id), builder.graphView),
			drawingViewBuilder.build()
		)

		assertEquals(1, result.componentIds.size)
		assertTrue(builder.graphView.getWithId(result.componentIds[0]) is EdgeView<*>)
		assertNull((builder.graphView.getWithId(result.componentIds[0]) as EdgeView<*>).origin)
		assertNull((builder.graphView.getWithId(result.componentIds[0]) as EdgeView<*>).destination)

		assertEquals(11 + 1, builder.graphView.drawables.size)
		assertEquals(2, builder.graphView.getDrawables { it is NodeView<*> }.size)
		checkReadWrite()
	}

	@Test
	fun shouldPasteNodeViewWithThreeEdgeViews() {
		service.paste(
			service.copy(builder.graphView.drawables.map { it.id }, builder.graphView),
			drawingViewBuilder.build()
		)

		assertEquals(11 + 11, builder.graphView.drawables.size)
		assertEquals(4, builder.graphView.getDrawables { it is NodeView<*> }.size)
		checkReadWrite()
	}

	// Check if resulting GraphView can be read back after being serialized (check for dangling references)
	private fun checkReadWrite() {
		StorableCloner.clone(GraphStorable(builder.graphView))
	}
}