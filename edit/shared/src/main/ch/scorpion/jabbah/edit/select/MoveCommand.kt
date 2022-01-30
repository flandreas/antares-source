package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.drawable.Movable
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand

class MoveCommand(
	editor: Editor,
	private val componentIds: Collection<Int>,
	private val offset: Point2D,
	private val children: List<Command> = emptyList()
) : AbstractCommand("edit.command.move", editor) {

	override fun execute() {
		Movable.moveBy(
			componentIds.map { editor!!.drawing.getWithId(it)!! }.toList(),
			offset)
		children.forEach { it.execute() }
	}
}