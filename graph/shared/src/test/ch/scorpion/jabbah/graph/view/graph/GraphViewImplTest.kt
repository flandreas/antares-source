package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.IOModule
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [GraphViewImpl].
 */
class GraphViewImplTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val graphView: GraphView<GraphElementView<*>> = GraphViewImpl(
		GraphImpl(),
		IOModule.storableClonerProvider.invoke(),
		BaseModule.eventBus)

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
}