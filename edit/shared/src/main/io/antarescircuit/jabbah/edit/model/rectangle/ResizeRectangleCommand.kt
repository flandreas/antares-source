package io.antarescircuit.jabbah.edit.model.rectangle

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.command.AbstractCommand

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