package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * A [Command] for moving an individual point of a [QuadCurveComponent].
 */
class MoveQuadCurvePointCommand(
	val curve: QuadCurveComponent,
	editor: Editor,
	val index: Int,
	val oldLocation: Point2D,
	val newLocation: Point2D
) : AbstractCommand("edit.model.polyline.movePoint", editor) {

	override fun execute() {
		curve.setPointAt(index, newLocation)
	}

	override fun undo() {
		curve.setPointAt(index, oldLocation)
	}
}
