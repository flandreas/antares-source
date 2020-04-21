package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.editor.AddCommand
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Connects an origin [Port] and a destination [Port] with a new [EdgeView].
 */
class ConnectCommand(
	editor: Editor,
	edgeView: EdgeView<*>,
	private val connectOriginCommand: Command?,
	private val connectDestinationCommand: Command?
) : AbstractCommand("graph.command.connect", editor) {

	private val addCommand = AddCommand(editor, edgeView)

	override fun execute() {
		addCommand.execute()
		connectOriginCommand?.execute()
		connectDestinationCommand?.execute()
	}

	override fun undo() {
		connectDestinationCommand?.undo()
		connectOriginCommand?.undo()
		addCommand.undo()
	}
}
