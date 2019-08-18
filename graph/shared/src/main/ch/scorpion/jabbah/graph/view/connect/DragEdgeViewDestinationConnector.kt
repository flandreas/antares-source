package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType.DESTINATION

class DragEdgeViewDestinationConnector(
	private val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService
) : AbstractDragEdgeViewEndpointConnector(DESTINATION) {

	override fun completeDragOpen(context: EditInputEventContext) {
		context.editor.commandManager.beginTransaction(createMoveCommand(context))
		context.editor.commandManager.commitTransaction()
	}

	override fun completeDragConnecting(context: EditInputEventContext) {
		context.editor.commandManager.beginTransaction(createMoveCommand(context))
		context.editor.commandManager.execute(createConnectCommand(context))
		context.editor.commandManager.commitTransaction()
	}

	private fun createMoveCommand(context: EditInputEventContext): Command {
		return MoveDestinationEndpointCommand(
			editor = context.editor,
			edgeView = edgeView!!,
			oldLocation = oldLocation,
			newLocation = edgeView!!.destinationEndpointView.location)
	}

	private fun createConnectCommand(context: EditInputEventContext): Command {
		return ConnectDestinationCommand(
			editor = context.editor,
			service = connectService,
			edgeView = edgeView!!,
			destConnectableView = targetPortView!!.owner!!,
			destPort = targetPortView!!.port)
	}

	override fun cancel(editor: Editor) {
		edgeView!!.moveDestinationEndPoint(oldLocation.x, oldLocation.y)
	}
}