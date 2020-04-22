package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DragEdgeViewOriginConnectorTest
	: AbstractInputEventHandlerTest(GraphViewModule.dragEdgeViewOriginConnector.handler) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	init {
		builder.connectInputOpen(v2, Point2D(150, 100))
		editor.commandManager.reset()
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

		assertConnected()
	}

	private fun assertConnected() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!

		assertTrue(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertEquals(newV1.getPortConnectionPoint(newV1.model.getOutput<Boolean>()), newEv.originEndpointView.location)
	}

	private fun assertOriginal() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!

		assertFalse(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertEquals(Point2D(150, 100), newEv.originEndpointView.location)
	}

	@Test
	fun shouldUndoConnect() {
		connect()

		editor.commandManager.undo()

		assertOriginal()
	}

	private fun connect() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(130, 100)
		releaseMouseAt(130, 100)
	}

	@Test
	fun shouldRedoConnect() {
		connect()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnected()
	}

	@Test
	fun shouldMoveOnly() {
		moveOnly()

		assertMoved()
	}

	private fun moveOnly() {
		mouseMoveTo(150, 100)
		pressMouseAt(150, 100)
		dragMouseTo(145, 100)
		releaseMouseAt(145, 100)
	}

	private fun assertMoved() {
		val newEv = builder.graphView.getEdgeViews().first()
		assertEquals(1, newEv.model.portsCount)
		assertEquals(Point2D(145, 100), newEv.originEndpointView.location)
	}

	@Test
	fun shouldUndoMoveOnly() {
		moveOnly()

		editor.commandManager.undo()

		assertOriginal()
	}

	@Test
	fun shouldRedoMoveOnly() {
		moveOnly()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertMoved()
	}
}