package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeEndpointView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Drag the destination [EdgeEndpointView] of an [EdgeView] onto the origin [EdgeEndpointView]
 * of another [EdgeView] to join the two [EdgeView]s.
 */
class JoinOpenDestinationEdgeViewsTest : AbstractJoinOpenEdgeViewTest() {

	init {
		handler = GraphViewModule.dragEdgeViewDestinationConnector.handler
		CurrentConnectMethod.defaultMethod = ConnectMethod.AutoLayout
	}

	@Test
	fun shouldHighlight() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)

		dragMouseTo(150, 200)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(150, 300)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)
	}

	/** Joins [EdgeView] at [v1] with [EdgeView] at [v3].*/
	private fun join() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(150, 200)
		releaseMouseAt(150, 200)
	}

	@Test
	fun shouldJoin() {
		join()
		assertJoined()
	}

	@Test
	fun shouldUndoJoin() {
		join()
		editor.commandManager.undo()

		val edgeViews = builder.graphView.getEdgeViews()
		assertEquals(2, edgeViews.size)
	}

	@Test
	fun shouldRedoJoin() {
		join()
		editor.commandManager.undo()
		editor.commandManager.redo()

		assertJoined()
	}
}