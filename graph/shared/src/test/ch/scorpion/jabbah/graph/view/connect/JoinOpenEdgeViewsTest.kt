package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JoinOpenEdgeViewsTest
	: AbstractInputEventHandlerTest(GraphViewModule.dragEdgeViewDestinationConnector.handler) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))

	init {
		builder.connectOutputOpen(v1, Point2D(150, 100))
		builder.connectInputOpen(v3, Point2D(150, 200))
		editor.commandManager.reset()
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

	@Test
	fun shouldJoin() {
		join()
		assertJoined()
	}

	private fun join() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(150, 200)
		releaseMouseAt(150, 200)
	}

	private fun assertJoined() {
		val edgeViews = builder.graphView.getEdgeViews()
		assertEquals(1, edgeViews.size)
		assertEquals(Point2D(120, 100), edgeViews[0].polyline.getPointAt(0))
		assertEquals(Point2D(150, 100), edgeViews[0].polyline.getPointAt(1))
		assertEquals(Point2D(150, 200), edgeViews[0].polyline.getPointAt(2))
		assertEquals(Point2D(200, 200), edgeViews[0].polyline.getPointAt(3))
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