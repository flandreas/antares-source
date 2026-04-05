package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.event.Modifier
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.graph.health.GraphViewConsistencyCheck
import io.antarescircuit.jabbah.graph.view.AbstractInputEventHandlerTest
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReconnectOriginConnectorTest : AbstractInputEventHandlerTest() {

	init {
		handler = GraphViewModule.reconnectOriginConnector.handler
		CurrentConnectMethod.defaultMethod = ConnectMethod.AutoLayout
		GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
		builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 100, 200))
	}

	@Test
	fun shouldReconnect() {
		reconnect()

		assertReconnected()
	}

	private fun reconnect() {
		mouseMoveTo(115, 100, modifiers = Modifier.Alt.mask)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(115, 100)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(150, 100)

		dragMouseTo(130, 200)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(130, 200)
	}

	private fun assertReconnected() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV3 = builder.graphView.getVerticeView("v3")!!

		assertFalse(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertTrue(newEv.model.isConnectedWith(newV3.model.getOutput()))
		assertEquals(newV3.getPortConnectionPoint(newV3.model.getOutput<Boolean>()), newEv.originEndpointView.location)

		GraphViewConsistencyCheck.execute(builder.graphView)
	}

	@Test
	fun shouldUndoReconnect() {
		editor.commandManager.reset()
		reconnect()

		editor.commandManager.undo()

		assertOriginal()
	}

	private fun assertOriginal() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV3 = builder.graphView.getVerticeView("v3")!!

		assertTrue(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertFalse(newEv.model.isConnectedWith(newV3.model.getOutput()))
		assertEquals(newV1.getPortConnectionPoint(newV1.model.getOutput<Boolean>()), newEv.originEndpointView.location)
	}

	@Test
	fun shouldRedoReconnect() {
		editor.commandManager.reset()
		reconnect()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertReconnected()
	}

	@Test
	fun shouldReconnectOpenEnded() {
		reconnectOpenEnded()

		assertReconnectedOpenEnded()
	}

	private fun reconnectOpenEnded() {
		mouseMoveTo(115, 100, modifiers = Modifier.Alt.mask)
		pressMouseAt(115, 100)
		dragMouseTo(150, 200)
		releaseMouseAt(150, 200)
	}

	private fun assertReconnectedOpenEnded() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!

		assertFalse(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertEquals(Point2D(150, 200), newEv.originEndpointView.location)
	}

	@Test
	fun shouldUndoReconnectOpenEnded() {
		editor.commandManager.reset()
		reconnectOpenEnded()

		editor.commandManager.undo()

		assertOriginal()
	}

	@Test
	fun shouldRedoReconnectOpenEnded() {
		editor.commandManager.reset()
		reconnectOpenEnded()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertReconnectedOpenEnded()
	}

	@Test
	fun shouldCancelDrag() {
		mouseMoveTo(115, 100)
		pressMouseAt(115, 100)
		dragMouseTo(150, 200)

		pressEscape()

		assertOriginal()
	}
}