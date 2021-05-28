package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.StorableCloner
import org.junit.Test
import kotlin.test.assertEquals

import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for copy/paste of selections containing various combinations of [EdgeView]s and [NodeView]s.
 */
class GraphViewCopyPasteServiceImplNodeTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val service = GraphViewCopyPasteService()
	private val builder: GraphViewBuilder<Boolean> = GraphViewBuilder()
	private val drawingViewBuilder = DrawingViewMockBuilder().withDrawing(builder.build())
	private val vv1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv1", 100, 100))
	private val vv2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv2", 200, 100))
	private val vv3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv3", 200, 200))
	private val vv4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv4", 200, 300))
	private val ev12 = builder.connect(vv1, vv2)
	private val split3 = builder.split(ev12, 0, Point2D(150, 100), vv3)
	private val split4 = builder.split(split3.newEdgeView, 0, Point2D(150, 200), vv4)

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

		assertEquals(1, result.components.size)
		assertTrue(result.components[0] is EdgeView<*>)
		assertNull((result.components[0] as EdgeView<*>).origin)
		assertNull((result.components[0] as EdgeView<*>).destination)

		assertEquals(11 + 1, builder.graphView.drawables.size)
		assertEquals(2, builder.graphView.getDrawables { it is NodeView<*> }.size)
		checkReadWrite()
	}

	@Test
	fun shouldPasteNodeViewWithThreeEdgeViews() {
		val result = service.paste(
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