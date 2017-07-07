package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.draw.polyline.Polyline
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * A [Command] for adding a point to a [Polyline].
 */
class AddPolylinePointCommand(
    val polyline: Polyline,
    editor: Editor,
    val index: Int,
    val location: Point2D
) : AbstractCommand("edit.model.polyline.addPoint", editor) {

    override fun execute() {
        polyline.addPointAt(index, location.x, location.y)
    }

    override fun undo() {
        polyline.removePoint(index)
    }
}