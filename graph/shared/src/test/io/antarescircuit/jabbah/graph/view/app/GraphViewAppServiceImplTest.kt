package io.antarescircuit.jabbah.graph.view.app

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphElementViewWrapper
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.*

@Suppress("UNCHECKED_CAST")
class GraphViewAppServiceImplTest {

	private val service = GraphViewAppServiceImpl()
	private val builder: GraphViewBuilder<Boolean>
	private val drawingView: DrawingView<GraphElementView<*>, GraphView>
	private val vv1: TestVerticeView
	private val vv2: TestVerticeView
	private val vv3: TestVerticeView

	init {
		GraphViewTestRule.configure()
		builder = GraphViewBuilder()
		drawingView = DrawingViewMockBuilder().withDrawing(builder.graphView).build()
		vv1 = builder.addVerticeView(TestVerticeView("vv1", loc = Point2D(100, 100)))
		vv2 = builder.addVerticeView(TestVerticeView("vv2", loc = Point2D(200, 100)))
		vv3 = builder.addVerticeView(TestVerticeView("vv3", loc = Point2D(200, 200)))

		EditModule.commandManager.bindDataHolder(builder)
	}

	@Test
	fun shouldUndoDeleteBeginNode() {
		val ev = builder.connect(vv1, vv2)
		val split = builder.split(ev, 0, Point2D(150, 100), vv3)
		EditModule.commandManager.reset()

		service.delete(listOf(split.newEdgeView), drawingView)

		EditModule.commandManager.undo()

		// Instances have been recreated while replaying from undo snapshot

		assertEquals(3, getNodeView().getEdgeViews().size)
		assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.origin!!.connectableView === getVerticeView("vv1") })
		assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.destination!!.connectableView === getVerticeView("vv2") })
		assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.destination!!.connectableView === getVerticeView("vv3") })
		assertEquals(Point2D(150, 100), getNodeView().getEdgeViews().first { it.destination!!.connectableView === getVerticeView("vv2") }.getSegmentPoint(0))
		assertEquals(Point2D(200, 100), getNodeView().getEdgeViews().first { it.destination!!.connectableView === getVerticeView("vv2") }.getSegmentPoint(1))
	}

	@Test
	fun shouldUndoDeleteInFrontOfNode() {
		val ev = builder.connect(vv1, vv2)
		builder.split(ev, 0, Point2D(150, 100), vv3)
		EditModule.commandManager.reset()

		service.delete(listOf(ev), drawingView)

		EditModule.commandManager.undo()

		assertEquals(3, getNodeView().getEdgeViews().size)
		assertEquals(Point2D(150, 100), getNodeView().location)
		assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.origin!!.connectableView === getVerticeView("vv1") })
		assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.destination!!.connectableView === getVerticeView("vv2") })
		assertNotNull(getNodeView().getEdgeViews().firstOrNull { it.destination!!.connectableView === getVerticeView("vv3") })
	}

	@Test
	fun shouldUndoDeleteOpenBehindNode() {
		val ev = builder.connectOutputOpen(vv1, Point2D(200, 100))
		val result = builder.split(ev, 0, Point2D(150, 100), null as PortView<Boolean>?)
		result.newEdgeView.moveDestinationEndPoint(200.0, 200.0)
		EditModule.commandManager.reset()

		service.delete(listOf(result.newEdgeView), drawingView)

		EditModule.commandManager.undo()

		val edgeViews = getNodeView().getEdgeViews()
		assertEquals(listOf(Point2D(150, 100), Point2D(150, 200), Point2D(200, 200)), edgeViews[0].polyline.getPoints(0, 3))
		assertEquals(listOf(Point2D(150, 100), Point2D(200, 100)), edgeViews[1].polyline.getPoints(0, 2))
		assertEquals(listOf(Point2D(100, 100), Point2D(150, 100)), edgeViews[2].polyline.getPoints(0, 2))
	}

	@Test
	fun shouldUndoDeleteAll() {
		val ev = builder.connect(vv1, vv2)
		builder.split(ev, 0, Point2D(150, 100), vv3)
		EditModule.commandManager.reset()

		service.delete(builder.graphView.drawables.toList(), DrawingViewMockBuilder().withDrawing(builder.graphView).build<GraphElementView<*>, GraphView>())

		EditModule.commandManager.undo()

		assertEquals(3, getNodeView().getEdgeViews().size)
	}

	@Test
	fun shouldAddWrapperWhenAddingNonGraphElementView() {
		service.add(RectangleComponent(), DrawingViewMockBuilder().withDrawing(builder.graphView).build())

		assertTrue(builder.graphView.get(0) is GraphElementViewWrapper)
		assertEquals(4, builder.graphView.drawables.size)
	}

	@Test
	fun shouldUndoAddWrappedComponent() {
		val component = RectangleComponent()
		service.add(component, DrawingViewMockBuilder().withDrawing(builder.graphView).build())

		EditModule.commandManager.undo()

		assertEquals(3, builder.graphView.drawables.size)
	}

	@Test
	fun shouldNotAddWrapperWhenAddingToNonGraphView() {
		val drawing = DrawingImpl<Component>()
		service.add(RectangleComponent(), DrawingViewMockBuilder().withDrawing(drawing).build())

		assertFalse(drawing.get(0) is GraphElementViewWrapper)
	}

	@Test
	fun shouldDeleteWrappedComponent() {
		val addedComponent = service.add(RectangleComponent(), drawingView as DrawingView<Component, Drawing<Component>>)

		service.delete(listOf(addedComponent), drawingView)

		assertEquals(3, builder.graphView.drawables.size)
	}

	@Test
	fun shouldUndoDeleteWrappedComponent() {
		val addedComponent = service.add(RectangleComponent(), drawingView as DrawingView<Component, Drawing<Component>>)
		EditModule.commandManager.reset()

		service.delete(listOf(addedComponent), drawingView)

		EditModule.commandManager.undo()

		assertTrue(builder.graphView.get(0) is GraphElementViewWrapper)
		assertEquals(4, builder.graphView.drawables.size)
	}

	@Test
	fun shouldRedoDeleteWrappedComponent() {
		EditModule.commandManager.reset()
		val addedComponent = service.add(RectangleComponent(), drawingView as DrawingView<Component, Drawing<Component>>)

		service.delete(listOf(addedComponent), drawingView)
		EditModule.commandManager.undo()
		EditModule.commandManager.redo()

		assertEquals(3, builder.graphView.drawables.size)
	}

	private fun getNodeView(): NodeView<*> {
		return builder.graphView.getDrawables { it is NodeView<*> }.map { it as NodeView<*> }.first()
	}

	private fun getVerticeView(name: String): VerticeView<*> {
		return builder.graphView.getVerticeView(name)!!
	}
}