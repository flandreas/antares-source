package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.Modifier
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.graph.health.GraphViewConsistencyCheck
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReconnectDestinationConnectorTest
	: AbstractInputEventHandlerTest(GraphViewModule.reconnectDestinationConnector.handler) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	init {
		CurrentConnectMethod.defaultMethod = ConnectMethod.AutoLayout
		GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
		builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
	}

	@Test
	fun shouldReconnect() {
		reconnect()

		assertReconnected()
	}

	private fun reconnect() {
		mouseMoveTo(205, 100, modifiers = Modifier.Alt.mask)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(205, 100)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(150, 100)

		dragMouseTo(200, 200)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(200, 200)
	}

	private fun assertReconnected() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV2 = builder.graphView.getVerticeView("v2")!!
		val newV3 = builder.graphView.getVerticeView("v3")!!

		assertFalse(newEv.model.isConnectedWith(newV2.model.getInput()))
		assertTrue(newEv.model.isConnectedWith(newV3.model.getInput()))
		assertEquals(newV3.getPortConnectionPoint(newV3.model.getInput<Boolean>()), newEv.destinationEndpointView.location)

		GraphViewConsistencyCheck.execute(builder.graphView)
	}

	@Test
	fun shouldLeaveWhenReleasingAltKey() {
		mouseMoveTo(205, 100, modifiers = Modifier.Alt.mask)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseKey(KeyEvent.VK_ALT)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)
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
		val newV2 = builder.graphView.getVerticeView("v2")!!
		val newV3 = builder.graphView.getVerticeView("v3")!!

		assertTrue(newEv.model.isConnectedWith(newV2.model.getInput()))
		assertFalse(newEv.model.isConnectedWith(newV3.model.getInput()))
		assertEquals(newV2.getPortConnectionPoint(newV2.model.getInput<Boolean>()), newEv.destinationEndpointView.location)
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
		mouseMoveTo(205, 100, modifiers = Modifier.Alt.mask)
		pressMouseAt(205, 100)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)
	}

	private fun assertReconnectedOpenEnded() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV2 = builder.graphView.getVerticeView("v2")!!

		assertFalse(newEv.model.isConnectedWith(newV2.model.getInput()))
		assertEquals(Point2D(150, 100), newEv.destinationEndpointView.location)
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
		mouseMoveTo(205, 100)
		pressMouseAt(205, 100)
		dragMouseTo(150, 100)

		pressEscape()

		assertOriginal()
	}

	@Test
	fun shouldAbortReconnectWhenReleasingOnOrigin() {
		editor.commandManager.reset()
		beginDragAndAbort()

		assertOriginal()
	}

	private fun beginDragAndAbort() {
		mouseMoveTo(205, 100)
		pressMouseAt(205, 100)
		releaseMouseAt(205, 100)
	}
}