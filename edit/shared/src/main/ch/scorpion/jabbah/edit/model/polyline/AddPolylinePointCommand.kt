package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.draw.polyline.Polyline
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable

/**
 * A [Command] for adding a point to a [Polyline].
 */
class AddPolylinePointCommand(
    editor: Editor,
    private val polylineId: Int,
    private val index: Int,
    private val location: Point2D
) : AbstractCommand("edit.model.polyline.addPoint", editor), Undoable {

	private val polyline: PolylineComponent get() = editor!!.drawing.getWithId(polylineId) as PolylineComponent

    override fun execute() {
        polyline.addPointAt(index, location.x, location.y)
    }

    override fun undo() {
        polyline.removePoint(index)
    }
}