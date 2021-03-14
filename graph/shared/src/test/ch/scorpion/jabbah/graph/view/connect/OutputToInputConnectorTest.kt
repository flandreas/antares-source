package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.ALT_MASK
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.port.TestPortView
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.verify
import kotlin.test.*

class OutputToInputConnectorTest
	: AbstractInputEventHandlerTest(GraphViewModule.outputToInputConnector.handler) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@Test
	fun shouldConnect() {
		mouseMoveTo(130, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(130, 100)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(150, 100)

		dragMouseTo(190, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(190, 100)

		assertConnected()
	}

	private fun assertConnected() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV2 = builder.graphView.getVerticeView("v2")!!

		assertTrue(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertTrue(newEv.model.isConnectedWith(newV2.model.getInput()))
	}

	/**
	 * Consider unconnected [TestPortView.LENGTH] that gets completely replaced by [EdgeView].
	 */
	@Test
	fun shouldCancelWhenReleasingNearbyStart() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(130 - TestPortView.LENGTH, 100)
		releaseMouseAt(130 - TestPortView.LENGTH, 100)

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
	}

	@Test
	fun shouldUndoConnect() {
		connect()

		editor.commandManager.undo()

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
	}

	private fun connect() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
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
	fun shouldConnectOpenEnded() {
		connectOpenEnded()

		assertConnectOpenEnded()
	}

	private fun assertConnectOpenEnded() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV2 = builder.graphView.getVerticeView("v2")!!

		assertTrue(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertFalse(newV2.model.getInput<Boolean>().isConnected)
		assertEquals(Point2D(150, 100), newEv.destinationEndpointView.location)
	}

	private fun connectOpenEnded() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)
	}

	@Test
	fun shouldUndoConnectOpenEnded() {
		connectOpenEnded()

		editor.commandManager.undo()

		assertUnconnected()
	}

	private fun assertUnconnected() {
		assertTrue(builder.graphView.getEdgeViews().isEmpty())
	}

	@Test
	fun shouldRedoConnectOpenEnded() {
		connectOpenEnded()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnectOpenEnded()
	}

	@Test
	fun shouldCancelWithEscapeOutsideTarget() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(150, 100)

		pressEscape()

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
		assertFalse(v1.model.getOutput<Boolean>().isConnected)
		assertFalse(v2.model.getInput<Boolean>().isConnected)
	}

	@Test
	fun shouldCancelWithEscapeInsideTarget() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(190, 100)

		pressEscape()

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
		assertFalse(v1.model.getOutput<Boolean>().isConnected)
		assertFalse(v2.model.getInput<Boolean>().isConnected)
	}

	@Test
	fun shouldAdjustDestination() {
		connectAdjusted()

		assertConnected()
		assertAdjustedConnectionGeometry()
	}

	private fun connectAdjusted() {
		mouseMoveTo(130, 100)
		clickMouseAt(130, 100, modifiers = ALT_MASK)

		mouseMoveTo(150, 200)
		clickMouseAt(150, 200)

		mouseMoveTo(170, 200)
		assertEquals(4, draggedEdgeView.segmentPointCount)

		clickMouseAt(170, 200)
		assertEquals(5, draggedEdgeView.segmentPointCount)

		mouseMoveTo(190, 100)
		assertEquals(6, draggedEdgeView.segmentPointCount)

		clickMouseAt(190, 100)
	}

	private fun assertAdjustedConnectionGeometry() {
		val newEv = builder.graphView.getEdgeViews().first()

		assertEquals(
			listOf(
				Point2D(120, 100),
				Point2D(150, 100),
				Point2D(150, 200),
				Point2D(170, 200),
				Point2D(170, 100),
				Point2D(200, 100)),
			newEv.polyline.getPoints(0, newEv.segmentPointCount)
		)
	}

	@Test
	fun shouldUndoAdjustDestination() {
		connectAdjusted()

		editor.commandManager.undo()

		assertUnconnected()
	}

	@Test
	fun shouldRedoAdjustDestination() {
		connectAdjusted()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnected()
		assertAdjustedConnectionGeometry()
	}

	@Test
	fun shouldCancelAdjustWithEscapePressed() {
		mouseMoveTo(130, 100)
		clickMouseAt(130, 100, modifiers = ALT_MASK)

		mouseMoveTo(150, 200)
		clickMouseAt(150, 200)

		mouseMoveTo(170, 200)
		assertEquals(4, draggedEdgeView.segmentPointCount)

		pressEscape()
		assertEquals(3, draggedEdgeView.segmentPointCount)

		pressEscape()
		assertTrue(builder.graphView.getEdgeViews().isEmpty())
	}

	@Test
	fun shouldEndAdjustOpenWithDoubleClick() {
		mouseMoveTo(130, 100)
		clickMouseAt(130, 100, modifiers = ALT_MASK)

		mouseMoveTo(150, 200)
		clickMouseAt(150, 200)

		mouseMoveTo(170, 200)
		doubleClickMouseAt(170, 200)

		assertTrue(draggedEdgeView.model.isConnectedWith(v1.model.getOutput()))
		assertNull(draggedEdgeView.destination)
		assertTrue(editor.commandManager.canUndo())
	}
}