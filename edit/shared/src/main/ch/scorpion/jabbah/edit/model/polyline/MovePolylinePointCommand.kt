package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.draw.polyline.Polyline
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.*

/**
 * A [Command] for moving an individual point of a [Polyline].
 */
class MovePolylinePointCommand(
	editor: Editor,
	val polylineId: Int,
	private val index: Int,
	private val oldLocation: Point2D,
	private val newLocation: Point2D
) : AbstractCommand("edit.model.polyline.movePoint", editor), Undoable {

	companion object {

		fun forOldLocation(
			editor: Editor,
			polyline: PolylineComponent,
			index: Int,
			oldLocation: Point2D
		): MovePolylinePointCommand {
			return MovePolylinePointCommand(editor, polyline.id, index, oldLocation, polyline.getPointAt(index))
		}

		fun forNewLocation(
			editor: Editor,
			polyline: PolylineComponent,
			index: Int,
			newLocation: Point2D
		): MovePolylinePointCommand {
			return MovePolylinePointCommand(editor, polyline.id, index, polyline.getPointAt(index), newLocation)
		}
	}

	private val polyline: PolylineComponent get() = editor!!.drawing.getWithId(polylineId)!!.selectableComponent as PolylineComponent

    override fun execute() {
        polyline.setPointAt(index, newLocation.x, newLocation.y)
    }

    override fun undo() {
        polyline.setPointAt(index, oldLocation.x, oldLocation.y)
    }
}