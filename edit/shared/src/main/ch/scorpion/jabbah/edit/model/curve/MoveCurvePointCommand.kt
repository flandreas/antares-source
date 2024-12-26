package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.command.AbstractDrawingViewCommand

/**
 * A [Command] for moving an individual point of an [AbstractCurveComponent].
 */
class MoveCurvePointCommand(
	val drawingView: DrawingView<*>,
	val curveId: Int,
	val index: Int,
	val oldLocation: Point2D,
	val newLocation: Point2D
) : AbstractDrawingViewCommand("edit.model.polyline.movePoint", drawingView), Undoable {

	private val curve: AbstractCurveComponent get() = drawingView.drawing.getWithId(curveId)!!.selectableComponent as AbstractCurveComponent

	override fun execute() {
		curve.setPointAt(index, newLocation)
	}

	override fun undo() {
		curve.setPointAt(index, oldLocation)
	}
}
