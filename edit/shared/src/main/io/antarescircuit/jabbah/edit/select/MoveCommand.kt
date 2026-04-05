package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.drawable.Movable
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.command.AbstractCommand

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