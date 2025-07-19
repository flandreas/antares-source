package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JoinOpenOriginEdgeViewTest
    : AbstractJoinOpenEdgeViewTest(GraphViewModule.dragEdgeViewOriginConnector.handler) {

    /** Joins [EdgeView] at [v3] with [EdgeView] at [v1].*/
    private fun join() {
        mouseMoveTo(150, 200)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        pressMouseAt(150, 200)

        dragMouseTo(150, 100)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        releaseMouseAt(150, 100)
    }

    @Test
    fun shouldJoin() {
        join()
        assertJoined()
    }

    @Test
    fun shouldUndoJoin() {
        join()
        editor.commandManager.undo()

        val edgeViews = builder.graphView.getEdgeViews()
        assertEquals(2, edgeViews.size)
    }

    @Test
    fun shouldRedoJoin() {
        join()
        editor.commandManager.undo()
        editor.commandManager.redo()

        assertJoined()
    }
}