package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.command.AbstractCommand

@Suppress("unused")
/**
 * Adds the previously copied [Component]s to a [Drawing].
 */
class PasteCommand(
    private val drawingView: DrawingView<Drawing<Component>>,
    private val components: List<Component>
) : AbstractCommand("graph.command.paste", null) {

    override fun execute() {
        for (c in components) {
            drawingView.drawing.add(c)
        }
        drawingView.selectionManager.deselectAll()
        drawingView.selectionManager.select(components)
    }

    override fun undo() {
        for (c in components) {
            drawingView.drawing.remove(c)
        }
    }
}