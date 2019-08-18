package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.verify
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReconnectOriginConnectorTest : AbstractConnectorTest(GraphViewModule.reconnectOriginConnector) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val ev: EdgeView<Boolean> = GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
	private val v3 = builder.addVerticeView(createEastOutputVerticeView(100, 200))

	@Test
	fun shouldReconnect() {
		mouseMoveTo(115, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(115, 100)
		kotlin.test.assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(150, 100)

		dragMouseTo(120, 200)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(120, 200)
		Assert.assertFalse(draggedEdgeView.model!!.isConnectedWith(v1.model!!.getOutput()))
		assertTrue(draggedEdgeView.model!!.isConnectedWith(v3.model!!.getOutput()))
		Assert.assertEquals(v3.getPortConnectionPoint(v3.model!!.getOutput<Boolean>()), draggedEdgeView.originEndpointView.location)
	}

	@Test
	fun shouldUndoReconnect() {
		mouseMoveTo(115, 100)
		pressMouseAt(115, 100)
		dragMouseTo(120, 200)
		releaseMouseAt(120, 200)

		editor.commandManager.undo()

		assertTrue(draggedEdgeView.model!!.isConnectedWith(v1.model!!.getOutput()))
		Assert.assertFalse(draggedEdgeView.model!!.isConnectedWith(v3.model!!.getOutput()))
		Assert.assertEquals(v1.getPortConnectionPoint(v1.model!!.getOutput<Boolean>()), draggedEdgeView.originEndpointView.location)
	}

	@Test
	fun shouldReconnectOpenEnded() {
		mouseMoveTo(115, 100)
		pressMouseAt(115, 100)
		dragMouseTo(150, 200)
		releaseMouseAt(150, 200)

		assertFalse(draggedEdgeView.model!!.isConnectedWith(v1.model!!.getOutput()))
		Assert.assertEquals(Point2D(150, 200), draggedEdgeView.originEndpointView.location)
	}

	@Test
	fun shouldUndoReconnectOpenEnded() {
		mouseMoveTo(115, 100)
		pressMouseAt(115, 100)
		dragMouseTo(150, 200)
		releaseMouseAt(150, 200)

		editor.commandManager.undo()

		assertTrue(draggedEdgeView.model!!.isConnectedWith(v1.model!!.getOutput()))
		Assert.assertEquals(v1.getPortConnectionPoint(v1.model!!.getOutput<Boolean>()), draggedEdgeView.originEndpointView.location)
	}

	@Test
	fun shouldCancelDrag() {
		mouseMoveTo(115, 100)
		pressMouseAt(115, 100)
		dragMouseTo(150, 200)

		cancelDrag()

		assertTrue(draggedEdgeView.model!!.isConnectedWith(v1.model!!.getOutput()))
		Assert.assertEquals(v1.getPortConnectionPoint(v1.model!!.getOutput<Boolean>()), draggedEdgeView.originEndpointView.location)
	}
}