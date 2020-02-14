package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.ALT_MASK
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EdgeToPortConnectorTest : AbstractConnectorTest(GraphViewModule.edgeToPortConnector) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val ev = GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
	private val v3 = builder.addVerticeView(createEastOutputVerticeView(200, 200))

	@Test
	fun shouldConnectToPortView() {
		mouseMoveTo(150, 100, modifiers = ALT_MASK)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
		verify { view.setCursor(Cursor.CROSSHAIR) }

		pressMouseAt(150, 100)
		assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

		dragMouseTo(150, 200)

		dragMouseTo(190, 200)
		assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

		releaseMouseAt(190, 200)

		assertTrue(ev.model.isConnectedWith(v1.model.getOutput()))
		assertTrue(ev.model.isConnectedWith(v2.model.getInput()))
		assertTrue(ev.model.isConnectedWith(v3.model.getInput()))

		// 3 VerticeViews, 1 NodeView, 3 EdgeViews
		kotlin.test.assertEquals(7, builder.graphView.drawablesCount)
	}

	@Test
	fun shouldUndoConnectToPortView() {
		mouseMoveTo(150, 100, modifiers = ALT_MASK)
		pressMouseAt(150, 100)
		dragMouseTo(190, 200)
		releaseMouseAt(190, 200)

		editor.commandManager.undo()

		assertTrue(ev.model.isConnectedWith(v1.model.getOutput()))
		assertTrue(ev.model.isConnectedWith(v2.model.getInput()))
		assertFalse(ev.model.isConnectedWith(v3.model.getInput()))

		// 3 VerticeViews, 1 EdgeView
		assertEquals(4, builder.graphView.drawablesCount)
	}

	@Test
	fun shouldConnectOpenEnded() {
		mouseMoveTo(150, 100, modifiers = ALT_MASK)
		pressMouseAt(150, 100)
		dragMouseTo(150, 200)
		releaseMouseAt(150, 200)

		assertTrue(ev.model.isConnectedWith(v1.model.getOutput()))
		assertTrue(ev.model.isConnectedWith(v2.model.getInput()))

		// 3 VerticeViews, 3 EdgeView, 1 NodeView
		assertEquals(7, builder.graphView.drawablesCount)
	}

	@Test
	fun shouldUndoConnectOpenEnded() {
		mouseMoveTo(150, 100, modifiers = ALT_MASK)
		pressMouseAt(150, 100)
		dragMouseTo(150, 200)
		releaseMouseAt(150, 200)

		editor.commandManager.undo()

		assertTrue(ev.model.isConnectedWith(v1.model.getOutput()))
		assertTrue(ev.model.isConnectedWith(v2.model.getInput()))

		// 3 VerticeViews, 1
		assertEquals(4, builder.graphView.drawablesCount)
	}

	@Test
	fun shouldCancelReleaseAtStartLocation() {
		mouseMoveTo(150, 100, modifiers = ALT_MASK)
		pressMouseAt(150, 100)
		releaseMouseAt(150, 100)

		// 3 VerticeViews, 1 EdgeView
		assertEquals(4, builder.graphView.drawablesCount)
	}
}