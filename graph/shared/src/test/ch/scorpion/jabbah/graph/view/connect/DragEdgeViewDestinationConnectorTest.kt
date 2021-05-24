package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.verify
import kotlin.test.*

class DragEdgeViewDestinationConnectorTest
	: AbstractInputEventHandlerTest(GraphViewModule.dragEdgeViewDestinationConnector.handler) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	init {
		builder.connectOutputOpen(v1, Point2D(150, 100))
		editor.commandManager.reset()
	}

	@Test
	fun shouldConnect() {
		mouseMoveTo(150, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(150, 100)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(170, 100)

		dragMouseTo(190, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(190, 100)

		assertConnected()
	}

	private fun assertConnected() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV2 = builder.graphView.getVerticeView("v2")!!

		assertTrue(newEv.model.isConnectedWith(newV2.model.getInput()))
		assertEquals(newV2.getPortConnectionPoint(newV2.model.getInput<Boolean>()), newEv.destinationEndpointView.location)
	}

	private fun assertOriginal() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV2 = builder.graphView.getVerticeView("v2")!!

		assertFalse(newEv.model.isConnectedWith(newV2.model.getInput()))
		assertEquals(Point2D(150, 100), newEv.destinationEndpointView.location)
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
		dragMouseTo(190, 100)
		releaseMouseAt(190, 100)
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
		dragMouseTo(170, 100)
		releaseMouseAt(170, 100)
	}

	private fun assertMoved() {
		val newEv = builder.graphView.getEdgeViews().first()

		assertEquals(1, newEv.model.portsCount)
		assertEquals(Point2D(170, 100), newEv.destinationEndpointView.location)
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

	// TODO Implement logic
	@Ignore
	@Test
	fun shouldDeleteHighlighterWhenEdgeViewIsDeleted() {
		val ev = builder.graphView.getEdgeViews().first()
		mouseMoveTo(150, 100)

		GraphViewModule.graphViewAppService.delete(listOf(ev), editor.view)

		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)
	}
}