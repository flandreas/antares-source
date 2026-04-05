package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.graph.view.EdgeView

/**
 * Moves the destination end point of an [EdgeView].
 */
class MoveDestinationEndpointCommand(
	editor: Editor,
	private val edgeViewId: Int,
	private val oldLocation: Point2D,
	private val newLocation: Point2D
) : AbstractCommand("edit.command.move", editor), Undoable {

	private val edgeView: EdgeView<*> get() = editor!!.drawing.getWithId(edgeViewId) as EdgeView<*>

	override fun getDetailedDescription(): String =
		"${super.getDetailedDescription()} $edgeViewId"

	override fun execute() {
		edgeView.moveDestinationEndPoint(newLocation.x, newLocation.y)
	}

	override fun undo() {
		edgeView.moveDestinationEndPoint(oldLocation.x, oldLocation.y)
	}
}