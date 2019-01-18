package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.draw.polyline.Polyline
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * A [Command] for moving an individual point of a [Polyline].
 */
class MovePolylinePointCommand(
	editor: Editor,
    private val polyline: Polyline,
    private val index: Int,
    private val newLocation: Point2D
) : AbstractCommand("edit.model.polyline.movePoint", editor) {

	private val oldLocation: Point2D = polyline.getPointAt(index)

    override fun execute() {
        polyline.setPointAt(index, newLocation.x, newLocation.y)
    }

    override fun undo() {
        polyline.setPointAt(index, oldLocation.x, oldLocation.y)
    }
}