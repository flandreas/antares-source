package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Moves the destination end point of an [EdgeView].
 */
class MoveDestinationEndpointCommand(
	editor: Editor,
	private val edgeView: EdgeView<*>,
	private val oldLocation: Point2D,
	private val newLocation: Point2D
) : AbstractCommand("edit.command.move", editor) {

	/** ---- [Command] interface */

	override fun execute() {
		edgeView.moveDestinationEndPoint(newLocation.x, newLocation.y)
	}

	override fun undo() {
		edgeView.moveDestinationEndPoint(oldLocation.x, oldLocation.y)
	}
}