package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * A [Command] for moving an individual point of a [QuadCurveComponent].
 */
class MoveQuadCurvePointCommand(
	val drawingView: DrawingView<*>,
	editor: Editor,
	val curveId: Int,
	val index: Int,
	val oldLocation: Point2D,
	val newLocation: Point2D
) : AbstractCommand("edit.model.polyline.movePoint", editor), Undoable {

	private val curve: QuadCurveComponent get() = drawingView.drawing.getWithId(curveId) as QuadCurveComponent

	override fun execute() {
		curve.setPointAt(index, newLocation)
	}

	override fun undo() {
		curve.setPointAt(index, oldLocation)
	}
}
