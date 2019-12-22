package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DragEdgeViewDestinationConnectorTest : AbstractConnectorTest(GraphViewModule.dragEdgeViewDestinationConnector) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	init {
		builder.connectOutputOpen(v1, Point2D(150, 100))
	}

	@Test
	fun shouldConnect() {
		mouseMoveTo(150, 100)
		kotlin.test.assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(150, 100)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(170, 100)

		dragMouseTo(190, 100)
		kotlin.test.assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(190, 100)
		assertTrue(draggedEdgeView.model!!.isConnectedWith(v2.model!!.getInput()))
		assertEquals(v2.getPortConnectionPoint(v2.model!!.getInput<Boolean>()), draggedEdgeView.destinationEndpointView.location)
	}

	@Test
	fun shouldUndoConnect() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(190, 100)
		releaseMouseAt(190, 100)

		editor.commandManager.undo()

		assertFalse(draggedEdgeView.model!!.isConnectedWith(v2.model!!.getInput()))
		assertEquals(Point2D(150, 100), draggedEdgeView.destinationEndpointView.location)
	}

	@Test
	fun shouldMoveOnly() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(170, 100)
		releaseMouseAt(170, 100)

		assertEquals(1, draggedEdgeView.model!!.portsCount)
		assertEquals(Point2D(170, 100), draggedEdgeView.destinationEndpointView.location)
	}

	@Test
	fun shouldUndoMoveOnly() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(170, 100)
		releaseMouseAt(170, 100)

		editor.commandManager.undo()

		assertEquals(1, draggedEdgeView.model!!.portsCount)
		assertEquals(Point2D(150, 100), draggedEdgeView.destinationEndpointView.location)
	}
}