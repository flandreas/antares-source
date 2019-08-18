package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OutputToInputConnectorTest : AbstractConnectorTest(GraphViewModule.outputToInputConnector) {

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
		assertTrue(draggedEdgeView.model!!.isConnectedWith(v1.model!!.getOutput()))
		assertTrue(draggedEdgeView.model!!.isConnectedWith(v2.model!!.getInput()))
	}

	@Test
	fun shouldUndoConnect() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(190, 100)
		releaseMouseAt(190, 100)

		editor.commandManager.undo()

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
		assertFalse(v1.model!!.getOutput<Boolean>().isConnected)
		assertFalse(v2.model!!.getInput<Boolean>().isConnected)
	}

	@Test
	fun shouldConnectOpenEnded() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)

		assertTrue(draggedEdgeView.model!!.isConnectedWith(v1.model!!.getOutput()))
		assertFalse(v2.model!!.getInput<Boolean>().isConnected)
	}

	@Test
	fun shouldUndoConnectOpenEnded() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(150, 100)
		releaseMouseAt(150, 100)

		editor.commandManager.undo()

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
		assertFalse(v1.model!!.getOutput<Boolean>().isConnected)
		assertFalse(v2.model!!.getInput<Boolean>().isConnected)
	}

	@Test
	fun shouldCancelOutsideTarget() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(150, 100)

		cancelDrag()

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
		assertFalse(v1.model!!.getOutput<Boolean>().isConnected)
		assertFalse(v2.model!!.getInput<Boolean>().isConnected)
	}

	@Test
	fun shouldCancelInsideTarget() {
		mouseMoveTo(130, 100)
		pressMouseAt(130, 100)
		dragMouseTo(190, 100)

		cancelDrag()

		assertTrue(builder.graphView.getEdgeViews().isEmpty())
		assertFalse(v1.model!!.getOutput<Boolean>().isConnected)
		assertFalse(v2.model!!.getInput<Boolean>().isConnected)
	}
}