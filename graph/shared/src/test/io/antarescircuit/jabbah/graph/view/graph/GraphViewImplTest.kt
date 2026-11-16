package io.antarescircuit.jabbah.graph.view.graph

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.graph.model.graph.GraphImpl
import io.antarescircuit.jabbah.graph.view.GraphElementViewWrapper
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewImpl
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GraphViewImplTest {

	private val graphView: GraphView

	init {
		GraphViewTestRule.configure()
		graphView = GraphViewImpl(GraphImpl(), BaseModule.eventBus)
	}

	@Test
	fun shouldAddToModel() {
		val verticeView = TestVerticeView()
		graphView.add(verticeView)

		assertTrue(graphView.contains(verticeView))
		assertTrue(graphView.graph!!.contains(verticeView.vertice))
	}

	@Test
	fun shouldRemoveFromModel() {
		val verticeView = TestVerticeView()
		graphView.add(verticeView)
		graphView.remove(verticeView)

		assertFalse(graphView.contains(verticeView))
		assertFalse(graphView.graph!!.contains(verticeView.vertice))
	}

	@Test
	fun shouldClearModel() {
		val verticeView = TestVerticeView()
		graphView.add(verticeView)
		graphView.clear()

		assertFalse(graphView.contains(verticeView))
		assertFalse(graphView.graph!!.contains(verticeView.vertice))
	}

	@Test
	fun shouldCreateOnlyOneNetViewPerNet() {
		val builder = GraphViewBuilder<Boolean>()
		val v1 = builder.addVerticeView(TestVerticeView(loc = Point2D(100, 100)))
		val v2 = builder.addVerticeView(TestVerticeView(loc = Point2D(200, 100)))
		val v3 = builder.addVerticeView(TestVerticeView(loc = Point2D(200, 200)))
		val origEdgeView = builder.connect(v1, v2)
		builder.split(origEdgeView, 0, Point2D(150, 100), v3)

		assertEquals(1, (builder.graphView as GraphViewImpl).getNetViewCount(origEdgeView.model))
	}

	@Test
	fun shouldKeepEdgesAndContainerFiguresBehindVerticesInCachedOrder() {
		val view = DrawingOrderGraphView()
		val vertex1 = TestVerticeView()
		val vertex2 = TestVerticeView()
		val edge1 = EdgeViewImpl<Boolean>()
		val edge2 = EdgeViewImpl<Boolean>()
		val rectangle = GraphElementViewWrapper(RectangleComponent())
		view.add(vertex1).add(edge1).add(rectangle).add(vertex2).add(edge2)

		val order = view.drawingOrder()

		assertEquals(listOf(edge1, rectangle, edge2, vertex1, vertex2), order)
		assertSame(order, view.drawingOrder())
	}

	@Test
	fun shouldReuseDrawingOrderAfterInvalidationAndGeometryUpdates() {
		val view = DrawingOrderGraphView()
		val vertex = TestVerticeView()
		view.add(vertex)
		val order = view.drawingOrder()

		vertex.invalidate()
		vertex.validate()
		vertex.location = Point2D(100, 100)
		vertex.update()

		assertSame(order, view.drawingOrder())
	}

	@Test
	fun shouldRefreshCachedDrawingOrderAfterAddingRemovingAndClearing() {
		val view = DrawingOrderGraphView()
		val vertex1 = TestVerticeView()
		val vertex2 = TestVerticeView()
		view.add(vertex1)
		assertEquals(listOf(vertex1), view.drawingOrder())

		view.add(vertex2)
		assertEquals(listOf(vertex1, vertex2), view.drawingOrder())
		view.remove(vertex1)
		assertEquals(listOf(vertex2), view.drawingOrder())
		view.clear()
		assertTrue(view.drawingOrder().isEmpty())
	}

	@Test
	fun shouldRefreshCachedDrawingOrderAfterRestackingWithinBothGroups() {
		val view = DrawingOrderGraphView()
		val vertex1 = TestVerticeView()
		val vertex2 = TestVerticeView()
		val rectangle1 = GraphElementViewWrapper(RectangleComponent())
		val rectangle2 = GraphElementViewWrapper(RectangleComponent())
		view.add(vertex1).add(rectangle1).add(vertex2).add(rectangle2)
		assertEquals(listOf(rectangle1, rectangle2, vertex1, vertex2), view.drawingOrder())

		view.setStackingOrderPosition(0, vertex1.id)
		view.setStackingOrderPosition(0, rectangle1.id)

		assertEquals(listOf(rectangle2, rectangle1, vertex2, vertex1), view.drawingOrder())
	}

	@Test
	fun shouldRefreshBackdropMembershipWhenWrappedComponentStyleChanges() {
		val view = DrawingOrderGraphView()
		val rectangle = RectangleComponent()
		val wrapper = GraphElementViewWrapper(rectangle)
		view.add(wrapper)
		assertTrue(view.getBackdropDrawables().isEmpty())
		val order = view.drawingOrder()

		rectangle.styleType = GraphStyleType.SUBSYSTEM
		assertEquals(listOf(wrapper), view.getBackdropDrawables())
		rectangle.styleType = StyleType.FIGURE
		assertTrue(view.getBackdropDrawables().isEmpty())
		assertSame(order, view.drawingOrder())
	}

	private class DrawingOrderGraphView : GraphViewImpl(GraphImpl(), BaseModule.eventBus) {
		fun drawingOrder() = drawablesInDrawingOrder()
	}
}
