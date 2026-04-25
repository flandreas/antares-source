package io.antarescircuit.jabbah.edit.model.curve

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.command.AbstractDrawingViewCommand

/**
 * A [Command] for moving an individual point of an [AbstractCurveComponent].
 */
class MoveCurvePointCommand(
	val drawingView: DrawingView<*,*>,
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
