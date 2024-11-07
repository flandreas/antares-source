package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * A [Command] for resizing a [RectangularShape].
 */
class ResizeRectangleCommand(
    editor: Editor,
    private val rectangleId: Int,
    private val oldBounds: Rectangle2D,
    private val newBounds: Rectangle2D
) : AbstractCommand("edit.model.rectangle.resize", editor), Undoable {

	private val rectangle: AbstractRectangularComponent get() =
        editor!!.drawing.getWithId(rectangleId)!!.selectableComponent as AbstractRectangularComponent

    override fun execute() {
        rectangle.setFrame(newBounds)
    }

    override fun undo() {
        rectangle.setFrame(oldBounds)
    }
}