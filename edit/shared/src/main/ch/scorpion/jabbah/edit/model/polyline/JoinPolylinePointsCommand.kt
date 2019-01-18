package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.draw.polyline.Polyline
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Editor

/**
 * A [Command] for joining two adjacent points of a [Polyline] by moving one of them onto the other one.
 *
 * @property polyline the [Polyline] whose points are joined
 * @property index the index of the point that has been moved
 * @property location the old location of the point that has been moved
 */
class JoinPolylinePointsCommand(
	editor: Editor,
	val polyline: Polyline,
	val index: Int,
	val oldLocation: Point2D
) : AbstractCommand("edit.model.polyline.joinPoints",editor) {

	override fun execute() {
		polyline.removePoint(index)
	}

	override fun undo() {
		polyline.addPointAt(index, oldLocation.x, oldLocation.y)
	}
}