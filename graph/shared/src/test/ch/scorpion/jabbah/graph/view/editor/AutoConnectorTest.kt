package ch.scorpion.jabbah.graph.view.editor

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AutoConnectorTest {

    private val builder: GraphViewBuilder<Boolean>
    private val v1: TestVerticeView
    private val v2: TestVerticeView
    private val editor: Editor

    init {
        GraphViewTestRule.configure()
        builder = GraphViewBuilder<Boolean>()
        v1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v1", 100, 100))
        v2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v2", 200, 100))
        editor = EditorImpl(DrawingViewImpl(builder.build() as Drawing<Component>))
    }

    @AfterTest
    fun cleanUp() {
        AutoConnector.handleDragTerminated(editor)
    }

    @Test
    fun shouldAllowOutputToInput() {
        v1.moveBy(60.0, 0.0)

        AutoConnector.handleDragged(editor, v1)

        val highlight = editor.view.animationContainer.drawables.first() as AutoConnectorHighlight
        assertEquals(1, highlight.points.size)
    }

    @Test
    fun shouldAllowInputToOutput() {
        v2.moveBy(-60.0, 0.0)

        AutoConnector.handleDragged(editor, v2)

        val highlight = editor.view.animationContainer.drawables.first() as AutoConnectorHighlight
        assertEquals(1, highlight.points.size)
    }

    @Test
    fun shouldDenySameTypes() {
        v1.moveBy(100.0, 0.0)

        AutoConnector.handleDragged(editor, v1)

        val highlight = editor.view.animationContainer.drawables.first() as AutoConnectorHighlight
        assertEquals(2, highlight.denyPoints.size)
    }
}