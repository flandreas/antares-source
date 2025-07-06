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

	override fun getDetailedDescription(): String {
		return if (componentIds.size == 1) {
			val out = StringBuilder()
			val id = componentIds.first()
			val component = editor!!.drawing.getWithId(id)!!
			out.appendLine("${super.getDetailedDescription()} ${component::class.simpleName} $id childrenSize:${children.size}")
			children.forEach {
				child -> out.appendLine("- child: ${child.getDetailedDescription()}")
			}
			out.toString()
		} else {
			super.getDetailedDescription()
		}
	}

	override fun execute() {
		Movable.moveBy(
			componentIds.map { editor!!.drawing.getWithId(it)!! }.toList(),
			offset)
		children.forEach { it.execute() }
	}
}