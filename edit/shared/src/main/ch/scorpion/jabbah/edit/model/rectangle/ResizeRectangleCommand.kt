package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape

/**
 * A [Command] for resizing a [RectangularShape].
 */
class ResizeRectangleCommand(
    editor: Editor,
    val rectangle: AbstractRectangularComponent,
    val oldBounds: Rectangle2D,
    val newBounds: Rectangle2D
) : AbstractCommand("edit.model.rectangle.resize", editor) {

    override fun execute() {
        rectangle.setFrame(newBounds)
    }

    override fun undo() {
        rectangle.setFrame(oldBounds)
    }
}