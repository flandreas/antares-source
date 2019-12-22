package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphElementViewWrapper
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.connect.SplitEdgeViewResult
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.*

/** Unit tests for [GraphViewServiceImpl].*/
class GraphViewServiceImplTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val service = GraphViewServiceImpl()
	private val builder = GraphViewBuilder<Boolean>()
	private val vv1 = builder.addVerticeView(TestVerticeView(loc = Point2D(100, 100)))
	private val vv2 = builder.addVerticeView(TestVerticeView(loc = Point2D(200, 100)))
	private val vv3 = builder.addVerticeView(TestVerticeView(loc = Point2D(200, 200)))
	private lateinit var ev: EdgeView<Boolean>
	private lateinit var split: SplitEdgeViewResult<Boolean>

	@Test
	fun shouldUndoDeleteBeginNode() {
		val ev = builder.connect(vv1, vv2)
		val split = builder.split(ev, 0, Point2D(150, 100), vv3)

		service.delete(listOf(split.newEdgeView), DrawingViewMockBuilder().withDrawing(builder.graphView).build())
		EditModule.commandManager.undo()

		assertEquals(3, (split.newEdgeView.origin?.connectableView as NodeView<Boolean>).getEdgeViews().size)
		assertEquals(3, getNodeView().getEdgeViews().size)
		assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.origin!!.connectableView === vv1 })
		assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.destination!!.connectableView === vv2 })
		assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.destination!!.connectableView === vv3 })
		assertEquals(Point2D(150, 100), getNodeView().getEdgeViews().first { it.destination!!.connectableView === vv2 }.getSegmentPoint(0))
		assertEquals(Point2D(200, 100), getNodeView().getEdgeViews().first { it.destination!!.connectableView === vv2 }.getSegmentPoint(1))
	}

	@Test
	fun shouldUndoDeleteInFrontOfNode() {
		val ev = builder.connect(vv1, vv2)
		builder.split(ev, 0, Point2D(150, 100), vv3)

		service.delete(listOf(ev), DrawingViewMockBuilder().withDrawing(builder.graphView).build())
		EditModule.commandManager.undo()

		assertEquals(3, getNodeView().getEdgeViews().size)
		assertEquals(Point2D(150, 100), getNodeView().location)
		assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.origin!!.connectableView === vv1 })
		assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.origin!!.connectableView === vv2 })
		assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.destination!!.connectableView === vv3 })
	}

	@Test
	fun shouldUndoDeleteOpenBehindNode() {
		val ev = builder.connectOutputOpen(vv1, Point2D(200, 100))
		val result = builder.split(ev, 0, Point2D(150, 100), null)
		result.newEdgeView.moveDestinationEndPoint(200.0, 200.0)

		service.delete(listOf(result.newEdgeView), DrawingViewMockBuilder().withDrawing(builder.graphView).build())
		EditModule.commandManager.undo()

		val edgeViews = getNodeView().getEdgeViews()
		assertEquals(listOf(Point2D(150, 100), Point2D(200, 100)), edgeViews[0].polyline.getPoints(0, 2))
		assertEquals(listOf(Point2D(150, 100), Point2D(150, 200), Point2D(200, 200)), edgeViews[1].polyline.getPoints(0, 3))
		assertEquals(listOf(Point2D(100, 100), Point2D(150, 100)), edgeViews[2].polyline.getPoints(0, 2))
	}

	@Test
	fun shouldUndoDeleteAll() {
		val ev = builder.connect(vv1, vv2)
		builder.split(ev, 0, Point2D(150, 100), vv3)

		service.delete(builder.graphView.getDrawables().toList(), DrawingViewMockBuilder().withDrawing(builder.graphView).build())
		EditModule.commandManager.undo()

		assertEquals(3, getNodeView().getEdgeViews().size)
	}

	@Test
	fun shouldAddWrapperWhenAddingNonGraphElementView() {
		service.add(RectangleComponent(), DrawingViewMockBuilder().withDrawing(builder.graphView).build())

		assertTrue(builder.graphView.get(0) is GraphElementViewWrapper<*>)
	}

	@Test
	fun shouldNotAddWrapperWhenAddingToNonGraphView() {
		val drawing = DrawingImpl<Component>()
		service.add(RectangleComponent(), DrawingViewMockBuilder().withDrawing(drawing).build())

		assertFalse(drawing.get(0) is GraphElementViewWrapper<*>)
	}

	private fun getNodeView(): NodeView<*> {
		return builder.graphView.getDrawables { it is NodeView<*> }.map { it as NodeView<*> }.first()
	}
}