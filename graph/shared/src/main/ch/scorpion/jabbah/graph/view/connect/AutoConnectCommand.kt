package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.select.MoveCommand
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Moves a [VerticeView] within a [Drawing] and optionally connects it with open-ended [EdgeView]s.
 */
/*
class AutoConnectCommand(
	editor: Editor,
	verticeViewId: Int,
	offset: Point2D,
	private val connectCommands: Collection<Command>
) : MoveCommand(editor, listOf(verticeViewId), offset) {

	init {
		connectCommands.forEach { it.execute() }
	}

	override fun execute() {
		super.execute()
		connectCommands.forEach { it.execute() }
	}
}
 */