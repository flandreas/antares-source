package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.ALT_MASK
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InputToOutputOrEdgeConnectorTest : AbstractConnectorTest(GraphViewModule.inputToOutputOrEdgeConnector) {

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
		assertTrue(draggedEdgeView.model!!.isConnectedWith(v1.model!!.getOutput()))
		assertTrue(draggedEdgeView.model!!.isConnectedWith(v2.model!!.getInput()))
	}

	@Test
	fun shouldUndoConnectToOutput() {
		mouseMoveTo(190, 100)
		pressMouseAt(190, 100)
		dragMouseTo(130, 100)
		releaseMouseAt(130, 100)

		editor.commandManager.undo()

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
		assertFalse(v1.model!!.getOutput<Boolean>().isConnected)
		assertFalse(v2.model!!.getInput<Boolean>().isConnected)
	}

	@Test
	fun shouldConnectOpenEnded() {
		mouseMoveTo(190, 100)
		pressMouseAt(190, 100)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)

		assertTrue(draggedEdgeView.model!!.isConnectedWith(v2.model!!.getInput()))
		assertFalse(v1.model!!.getOutput<Boolean>().isConnected)
	}

	@Test
	fun shouldUndoConnectOpenEnded() {
		mouseMoveTo(190, 100)
		pressMouseAt(190, 100)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)

		editor.commandManager.undo()

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
		assertFalse(v1.model!!.getOutput<Boolean>().isConnected)
		assertFalse(v2.model!!.getInput<Boolean>().isConnected)
	}

	@Test
	fun shouldConnectToEdgeView() {
		val ev = GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
		val v3 = builder.addVerticeView(createEastOutputVerticeView(200, 200))

		mouseMoveTo(190, 200)
		pressMouseAt(190, 200)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)

		assertTrue(ev.model!!.isConnectedWith(v1.model!!.getOutput()))
		assertTrue(ev.model!!.isConnectedWith(v2.model!!.getInput()))
		assertTrue(ev.model!!.isConnectedWith(v3.model!!.getInput()))

		// 3 VerticeViews, 1 NodeView, 3 EdgeViews
		assertEquals(7, builder.graphView.drawablesCount)

		val ev2 = builder.graphView.getEdgeView(v3.model!!.getInput<Boolean>())!!
		assertEquals(Point2D(150, 100), ev2.polyline.getPointAt(0))
		assertEquals(Point2D(150, 200), ev2.polyline.getPointAt(1))
		assertEquals(Point2D(200, 200), ev2.polyline.getPointAt(2))
	}

	@Test
	fun shouldUndoConnectToEdgeView() {
		val ev = GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
		val v3 = builder.addVerticeView(createEastOutputVerticeView(200, 200))

		mouseMoveTo(190, 200)
		pressMouseAt(190, 200)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)

		editor.commandManager.undo()

		assertTrue(ev.model!!.isConnectedWith(v1.model!!.getOutput()))
		assertTrue(ev.model!!.isConnectedWith(v2.model!!.getInput()))
		assertFalse(ev.model!!.isConnectedWith(v3.model!!.getInput()))

		// 3 VerticeViews, 1 EdgeView
		assertEquals(4, builder.graphView.drawablesCount)
	}

	@Test
	fun shouldAdjustToPortView() {
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
		assertTrue(draggedEdgeView.model!!.isConnectedWith(v1.model!!.getOutput()))
		assertTrue(draggedEdgeView.model!!.isConnectedWith(v2.model!!.getInput()))
	}

	@Test
	fun shouldUndoAdjustmentWithEscapePressed() {
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
		val ev = GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
		val v3 = builder.addVerticeView(createEastOutputVerticeView(200, 200))

		mouseMoveTo(190, 200)
		clickMouseAt(190, 200, modifiers = ALT_MASK)

		mouseMoveTo(150, 200)
		clickMouseAt(150, 200)

		mouseMoveTo(150, 100)
		clickMouseAt(150, 100)

		assertTrue(ev.model!!.isConnectedWith(v1.model!!.getOutput()))
		assertTrue(ev.model!!.isConnectedWith(v2.model!!.getInput()))
		assertTrue(ev.model!!.isConnectedWith(v3.model!!.getInput()))

		// 3 VerticeViews, 1 NodeView, 3 EdgeViews
		assertEquals(7, builder.graphView.drawablesCount)
	}
}