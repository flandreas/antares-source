package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.test.assertTrue

class ReconnectDestinationConnectorTest : AbstractConnectorTest(GraphViewModule.reconnectDestinationConnector) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	init {
		GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
	}

	private val v3 = builder.addVerticeView(createEastOutputVerticeView(200, 200))

	@Test
	fun shouldReconnect() {
		mouseMoveTo(205, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(205, 100)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(150, 100)

		dragMouseTo(200, 200)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(200, 200)
		assertFalse(draggedEdgeView.model!!.isConnectedWith(v2.model!!.getInput()))
		assertTrue(draggedEdgeView.model!!.isConnectedWith(v3.model!!.getInput()))
		assertEquals(v3.getPortConnectionPoint(v3.model!!.getInput<Boolean>()), draggedEdgeView.destinationEndpointView.location)
	}

	@Test
	fun shouldUndoReconnect() {
		mouseMoveTo(205, 100)
		pressMouseAt(205, 100)
		dragMouseTo(200, 100)
		releaseMouseAt(200, 200)

		editor.commandManager.undo()

		assertTrue(draggedEdgeView.model!!.isConnectedWith(v2.model!!.getInput()))
		assertFalse(draggedEdgeView.model!!.isConnectedWith(v3.model!!.getInput()))
		assertEquals(v2.getPortConnectionPoint(v2.model!!.getInput<Boolean>()), draggedEdgeView.destinationEndpointView.location)
	}

	@Test
	fun shouldReconnectOpenEnded() {
		mouseMoveTo(205, 100)
		pressMouseAt(205, 100)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)

		assertFalse(draggedEdgeView.model!!.isConnectedWith(v2.model!!.getInput()))
		assertEquals(Point2D(150, 100), draggedEdgeView.destinationEndpointView.location)
	}

	@Test
	fun shouldUndoReconnectOpenEnded() {
		mouseMoveTo(205, 100)
		pressMouseAt(205, 100)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)

		editor.commandManager.undo()

		assertTrue(draggedEdgeView.model!!.isConnectedWith(v2.model!!.getInput()))
		assertEquals(v2.getPortConnectionPoint(v2.model!!.getInput<Boolean>()), draggedEdgeView.destinationEndpointView.location)
	}

	@Test
	fun shouldCancelDrag() {
		mouseMoveTo(205, 100)
		pressMouseAt(205, 100)
		dragMouseTo(150, 100)

		cancelDrag()

		assertTrue(draggedEdgeView.model!!.isConnectedWith(v2.model!!.getInput()))
		assertEquals(v2.getPortConnectionPoint(v2.model!!.getInput<Boolean>()), draggedEdgeView.destinationEndpointView.location)
	}
}