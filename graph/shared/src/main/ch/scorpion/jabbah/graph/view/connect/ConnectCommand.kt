package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.editor.AddCommand
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Connects an origin [Port] and a destination [Port] with a new [EdgeView].
 */
class ConnectCommand(
	editor: Editor,
	private val connectService: GraphViewConnectService,
	private val edgeView: EdgeView<*>,
	private val origConnectableView: ConnectableView?,
	private val origPort: Port<*>?,
	private val destConnectableView: ConnectableView?,
	private val destPort: Port<*>?
) : AbstractCommand("graph.command.connect", editor) {

	override fun addedToTransaction() {
		editor!!.commandManager.execute(AddCommand(editor, edgeView))
		if (origConnectableView != null) {
			editor.commandManager.execute(ConnectOriginCommand(editor, connectService, edgeView, origConnectableView, origPort!!))
		}
		if (destConnectableView != null) {
			editor.commandManager.execute(ConnectDestinationCommand(editor, connectService, edgeView, destConnectableView, destPort!!))
		}
	}

	override fun execute() {
		// empty
	}

	override fun undo() {
		// empty
	}
}