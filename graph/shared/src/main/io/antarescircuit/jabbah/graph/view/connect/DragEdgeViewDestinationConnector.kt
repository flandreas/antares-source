package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType.DESTINATION

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
		if (oldLocation != edgeView!!.destinationEndpointView.location) {
			LOG.userTrail("Move EdgeView ${edgeView?.id} destination endpoint open-ended to ${edgeView!!.destinationEndpointView.location}")
			context.editor.commandManager.execute(createMoveCommand(context))
		}
	}

	override fun completeDragConnecting(context: EditInputEventContext) {
		LOG.userTrail("Move EdgeView ${edgeView?.id} endpoint to connect port ${targetPortView?.port?.portId} of ${targetPortView?.owner?.id}")
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