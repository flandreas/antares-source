package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.ALT_MASK
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InputToOutputOrEdgeConnectorTest
	: AbstractInputEventHandlerTest(GraphViewModule.inputToOutputOrEdgeConnector.handler) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@Test
	fun shouldConnectToOutput() {
		mouseMoveTo(190, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(190, 100)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(150, 100)

		dragMouseTo(130, 100)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(110, 100)

		assertConnected()
	}

	private fun assertConnected() {
		val newEv = builder.graphView.getEdgeViews().first()
		val v1 = builder.graphView.getVerticeView("v1")!!
		val v2 = builder.graphView.getVerticeView("v2")!!

		assertTrue(newEv.model.isConnectedWith(v1.model.getOutput()))
		assertTrue(newEv.model.isConnectedWith(v2.model.getInput()))
	}

	@Test
	fun shouldUndoConnectToOutput() {
		connectToOutput()

		editor.commandManager.undo()

		assertOriginal()
	}

	private fun assertOriginal() {
		assertTrue(builder.graphView.getEdgeViews().isEmpty())
	}

	private fun connectToOutput() {
		mouseMoveTo(190, 100)
		pressMouseAt(190, 100)
		dragMouseTo(130, 100)
		releaseMouseAt(130, 100)
	}

	@Test
	fun shouldRedoConnectToOutput() {
		connectToOutput()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnected()
	}

	@Test
	fun shouldConnectOpenEnded() {
		connectOpenEnded()

		assertConnectedOpenEnded()
	}

	private fun connectOpenEnded() {
		mouseMoveTo(190, 100)
		pressMouseAt(190, 100)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)
	}

	private fun assertConnectedOpenEnded() {
		val newEv = builder.graphView.getEdgeViews().first()
		val v1 = builder.graphView.getVerticeView("v1")!!
		val v2 = builder.graphView.getVerticeView("v2")!!

		assertFalse(newEv.model.isConnectedWith(v1.model.getOutput()))
		assertTrue(newEv.model.isConnectedWith(v2.model.getInput()))
		assertEquals(Point2D(150, 100), newEv.originEndpointView.location)
	}

	@Test
	fun shouldUndoConnectOpenEnded() {
		connectOpenEnded()

		editor.commandManager.undo()

		assertOriginal()
	}

	@Test
	fun shouldRedoConnectOpenEnded() {
		connectOpenEnded()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnectedOpenEnded()
	}

	@Test
	fun shouldConnectToEdgeView() {
		connectToEdgeView()

		assertConnectedToEdgeView()
	}

	private fun connectToEdgeView() {
		GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
		builder.addVerticeView(createEastOutputVerticeView("v3", 200, 200))

		editor.commandManager.reset()

		mouseMoveTo(190, 200)
		pressMouseAt(190, 200)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)
	}

	private fun assertConnectedToEdgeView() {
		val nodeView = builder.graphView.getDrawable { it is NodeView<*> } as NodeView<Boolean>
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV2 = builder.graphView.getVerticeView("v2")!!
		val newV3 = builder.graphView.getVerticeView("v3")!!

		assertTrue(nodeView.model.isConnectedWith(newV1.model.getOutput()))
		assertTrue(nodeView.model.isConnectedWith(newV2.model.getInput()))
		assertTrue(nodeView.model.isConnectedWith(newV3.model.getInput()))

		// 3 VerticeViews, 1 NodeView, 3 EdgeViews
		assertEquals(7, builder.graphView.drawablesCount)
	}

	private fun assertAutoLayoutGeometry() {
		val nodeView = builder.graphView.getDrawable { it is NodeView<*> } as NodeView<Boolean>
		val newEv = nodeView.getEdgeView(Direction.SOUTH)!!

		assertEquals(Point2D(150, 100), newEv.polyline.getPointAt(0))
		assertEquals(Point2D(150, 200), newEv.polyline.getPointAt(1))
		assertEquals(Point2D(200, 200), newEv.polyline.getPointAt(2))
	}

	@Test
	fun shouldUndoConnectToEdgeView() {
		connectToEdgeView()

		editor.commandManager.undo()

		assertOriginalEdgeView()
	}

	private fun assertOriginalEdgeView() {
		// Instances have been recreated while replaying from undo snapshot
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV2 = builder.graphView.getVerticeView("v2")!!
		val newV3 = builder.graphView.getVerticeView("v3")!!

		assertTrue(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertTrue(newEv.model.isConnectedWith(newV2.model.getInput()))
		assertFalse(newEv.model.isConnectedWith(newV3.model.getInput()))

		// 3 VerticeViews, 1 EdgeView
		assertEquals(4, builder.graphView.drawablesCount)
	}

	@Test
	fun shouldRedoConnectToEdgeView() {
		connectToEdgeView()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnectedToEdgeView()
		assertAutoLayoutGeometry()
	}

	@Test
	fun shouldAdjustToPortView() {
		adjustToPortView()

		assertAdjustToPortView()
	}

	private fun adjustToPortView() {
		mouseMoveTo(190, 100)
		clickMouseAt(190, 100, modifiers = ALT_MASK)

		mouseMoveTo(160, 200)
		clickMouseAt(160, 200)

		mouseMoveTo(150, 200)
		assertEquals(4, draggedEdgeView.segmentPointCount)

		clickMouseAt(150, 200)
		assertEquals(5, draggedEdgeView.segmentPointCount)

		mouseMoveTo(130, 100)
		assertEquals(6, draggedEdgeView.segmentPointCount)

		clickMouseAt(130, 100)
	}

	private fun assertAdjustToPortView() {
		val newEv = builder.graphView.getEdgeViews().first()
		val newV1 = builder.graphView.getVerticeView("v1")!!
		val newV2 = builder.graphView.getVerticeView("v2")!!

		assertTrue(newEv.model.isConnectedWith(newV1.model.getOutput()))
		assertTrue(newEv.model.isConnectedWith(newV2.model.getInput()))

		assertEquals(
			listOf(
				Point2D(120, 100),
				Point2D(150, 100),
				Point2D(150, 200),
				Point2D(160, 200),
				Point2D(160, 100),
				Point2D(200, 100)),
			newEv.polyline.getPoints(0, newEv.segmentPointCount)
		)
	}

	@Test
	fun shouldUndoAdjustToPortView() {
		adjustToPortView()

		editor.commandManager.undo()

		assertOriginal()
	}

	@Test
	fun shouldRedoAdjustToPortView() {
		adjustToPortView()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertAdjustToPortView()
	}

	@Test
	fun shouldCancelAdjustmentWithEscapePressed() {
		mouseMoveTo(190, 100)
		clickMouseAt(190, 100, modifiers = ALT_MASK)

		mouseMoveTo(160, 200)
		clickMouseAt(160, 200)

		mouseMoveTo(150, 200)
		assertEquals(4, draggedEdgeView.segmentPointCount)

		pressEscape()
		assertEquals(3, draggedEdgeView.segmentPointCount)

		pressEscape()
		assertTrue(builder.graphView.getEdgeViews().isEmpty())
	}

	@Test
	fun shouldAdjustToEdgeView() {
		adjustToEdgeView()

		assertConnectedToEdgeView()
		assertAdjustedToEdgeViewGeometry()
	}

	private fun adjustToEdgeView() {
		GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
		builder.addVerticeView(createEastOutputVerticeView("v3", 200, 200))

		editor.commandManager.reset()

		mouseMoveTo(190, 200)
		clickMouseAt(190, 200, modifiers = ALT_MASK)

		mouseMoveTo(150, 200)
		clickMouseAt(150, 200)

		mouseMoveTo(150, 100)
		clickMouseAt(150, 100)
	}

	private fun assertAdjustedToEdgeViewGeometry() {
		val nodeView = builder.graphView.getDrawable { it is NodeView<*> } as NodeView<Boolean>
		val newEv = nodeView.getEdgeView(Direction.SOUTH)!!

		assertEquals(Point2D(150, 100), newEv.polyline.getPointAt(0))
		assertEquals(Point2D(150, 200), newEv.polyline.getPointAt(1))
		assertEquals(Point2D(200, 200), newEv.polyline.getPointAt(2))
	}

	@Test
	fun shouldUndoAdjustToEdgeView() {
		adjustToEdgeView()

		editor.commandManager.undo()

		assertOriginalEdgeView()
	}

	@Test
	fun shouldRedoAdjustToEdgeView() {
		adjustToEdgeView()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertConnectedToEdgeView()
		assertAdjustedToEdgeViewGeometry()
	}
}