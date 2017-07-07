package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.edit.tool.ToolAdapter
import ch.scorpion.jabbah.edit.Tool
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.editor.AddCommand
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor

/**
 * A [Tool] for interactively creating a [LabelComponent] in a [Drawing].
 */
class LabelTool(
    editor: Editor,
    val factory: () -> LabelComponent
) : ToolAdapter(editor) {

    override fun activate() {
        editor.view.setCursor(Cursor.TEXT)
    }

    override fun mousePressed(e: MouseEvent, x: Double, y: Double) {
        super.mousePressed(e, x, y)

        val label = factory.invoke()

        // snap the mouse pressed location
        val offset = editor.snapManager.snap(x, y)
        label.location = Point2D(x + offset.x, y + offset.y)

        editor.commandManager.beginTransaction(AddCommand(editor, label))
        editor.commandManager.commitTransaction()

        editor.view.selectionManager.deselectAll()
        editor.view.selectionManager.select(label)

        editor.toolDone()
    }
}