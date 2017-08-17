package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * A [Command] for deleting the selected [Component]s from a [Drawing].
 * TODO How can we preserve the original stacking order of the removed Components?
 */
class DeleteCommand(
        val drawingView: DrawingView<Drawing<Component>>,
        private val components: List<Component>
) : AbstractCommand("edit.command.delete", null) {

    override fun execute() {
        for (c in components) {
            drawingView.drawing.remove(c)
        }
    }

    override fun undo() {
        for (c in components) {
           drawingView.drawing.add(c)
        }
        drawingView.selectionManager.select(components)
    }

    override fun validate() {
        drawingView.drawing.validate()
    }
}