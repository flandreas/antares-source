package io.antarescircuit.jabbah.edit.model.polyline

import io.antarescircuit.jabbah.draw.polyline.Polyline
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.Undoable

/**
 * A [Command] for adding a point to a [Polyline].
 */
class AddPolylinePointCommand(
    editor: Editor,
    private val polylineId: Int,
    private val index: Int,
    private val location: Point2D
) : AbstractCommand("edit.model.polyline.addPoint", editor), Undoable {

	private val polyline: PolylineComponent get() = editor!!.drawing.getWithId(polylineId)!!.selectableComponent as PolylineComponent

    override fun execute() {
        polyline.addPointAt(index, location.x, location.y)
    }

    override fun undo() {
        polyline.removePoint(index)
    }
}