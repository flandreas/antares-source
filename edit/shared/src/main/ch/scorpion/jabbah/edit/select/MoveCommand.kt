package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand

open class MoveCommand(
	editor: Editor,
	private val componentIds: Collection<Int>,
	val offset: Point2D
) : AbstractCommand("edit.command.move", editor) {

	override fun execute() {
		val components = componentIds.map { editor!!.drawing.getWithId(it)!! }.toList()
		components.forEach { it.prepareMoveBy(components) }
		components.forEach { it.moveBy(offset.x, offset.y) }
		components.forEach { it.completeMoveBy() }
	}
}