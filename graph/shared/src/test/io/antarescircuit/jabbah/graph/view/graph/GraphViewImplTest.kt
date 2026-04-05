package io.antarescircuit.jabbah.graph.view.graph

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.model.graph.GraphImpl
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
}