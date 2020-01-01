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

class DragEdgeViewOriginConnectorTest : AbstractConnectorTest(GraphViewModule.dragEdgeViewOriginConnector) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	init {
		builder.connectInputOpen(v2, Point2D(150, 100))
	}

	@Test
	fun shouldConnect() {
		mouseMoveTo(150, 100)
		kotlin.test.assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(150, 100)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(140, 100)

		dragMouseTo(130, 100)
		kotlin.test.assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(130, 100)
		assertTrue(draggedEdgeView.model.isConnectedWith(v1.model.getOutput()))
		assertEquals(v1.getPortConnectionPoint(v1.model.getOutput<Boolean>()), draggedEdgeView.originEndpointView.location)
	}

	@Test
	fun shouldUndoConnect() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(130, 100)
		releaseMouseAt(130, 100)

		editor.commandManager.undo()

		assertFalse(draggedEdgeView.model.isConnectedWith(v1.model.getOutput()))
		assertEquals(Point2D(150, 100), draggedEdgeView.originEndpointView.location)
	}

	@Test
	fun shouldMoveOnly() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(145, 100)
		releaseMouseAt(145, 100)

		assertEquals(1, draggedEdgeView.model.portsCount)
		assertEquals(Point2D(145, 100), draggedEdgeView.originEndpointView.location)
	}

	@Test
	fun shouldUndoMoveOnly() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(145, 100)
		releaseMouseAt(145, 100)

		editor.commandManager.undo()

		assertEquals(1, draggedEdgeView.model.portsCount)
		assertEquals(Point2D(150, 100), draggedEdgeView.originEndpointView.location)
	}
}