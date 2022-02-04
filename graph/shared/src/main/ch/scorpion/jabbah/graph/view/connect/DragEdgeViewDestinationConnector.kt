package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType.DESTINATION

/**
 * Drags the open destination endpoint of an [EdgeView] to either another open location
 * or to be connected with a target [PortView].
 */
class DragEdgeViewDestinationConnector(
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService
) : AbstractDragEdgeViewEndpointConnector(connectService, DESTINATION) {

	companion object {
		private val LOG by logger(DragEdgeViewDestinationConnector::class)
	}

	override fun completeDragOpen(context: EditInputEventContext) {
		LOG.debug("Move EdgeView endpoint open-ended")
		context.editor.commandManager.beginTransaction(createMoveCommand(context))
		context.editor.commandManager.commitTransaction()
	}

	override fun completeDragConnecting(context: EditInputEventContext) {
		LOG.debug("Move EdgeView endpoint to connect port of ${targetPortView?.owner?.type}")
		context.editor.commandManager.beginTransaction(createMoveCommand(context))
		context.editor.commandManager.execute(createConnectCommand(context))
		context.editor.commandManager.commitTransaction()
	}

	private fun createMoveCommand(context: EditInputEventContext): Command {
		return MoveDestinationEndpointCommand(
			editor = context.editor,
			edgeViewId = edgeView!!.id,
			oldLocation = oldLocation,
			newLocation = edgeView!!.destinationEndpointView.location)
	}

	private fun createConnectCommand(context: EditInputEventContext): Command {
		return ConnectDestinationCommand(
			editor = context.editor,
			service = connectService,
			edgeViewId = edgeView!!.id,
			destConnectableViewId = targetPortView!!.owner!!.id,
			destPortId = targetPortView!!.port.portId)
	}

	override fun cancel(editor: Editor) {
		edgeView!!.moveDestinationEndPoint(oldLocation.x, oldLocation.y)
	}
}