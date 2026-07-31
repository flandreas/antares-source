package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType

class DragEdgeViewOriginConnector(
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService
) : AbstractDragEdgeViewEndpointConnector(connectService, EdgeViewEndpointType.ORIGIN) {

	companion object {
		private val LOG by logger(DragEdgeViewOriginConnector::class)
	}

	override fun completeDragOpen(context: EditInputEventContext) {
		if (oldLocation != edgeView!!.originEndpointView.location) {
			LOG.userTrail("Move EdgeView ${edgeView?.id} origin endpoint open-ended to ${edgeView!!.originEndpointView.location}")
			context.editor.commandManager.execute(createMoveCommand(context))
		}
	}

	override fun completeDragConnecting(context: EditInputEventContext) {
		LOG.userTrail("Move EdgeView endpoint to connect port ${targetPortView?.port?.portId} of ${targetPortView?.owner?.id}")
		context.editor.commandManager.beginTransaction(createMoveCommand(context))
		context.editor.commandManager.execute(createConnectCommand(context))
		context.editor.commandManager.commitTransaction()
	}

	private fun createMoveCommand(context: EditInputEventContext): Command {
		return MoveOriginEndpointCommand(
			editor = context.editor,
			edgeViewId = edgeView!!.id,
			oldLocation = oldLocation,
			newLocation = edgeView!!.originEndpointView.location)
	}

	private fun createConnectCommand(context: EditInputEventContext): Command {
		return ConnectOriginCommand(
			editor = context.editor,
			service = connectService,
			edgeViewId = edgeView!!.id,
			origConnectableViewId = targetPortView!!.owner!!.id,
			origPortId = targetPortView!!.port.portId)
	}

	override fun cancel(editor: Editor) {
		edgeView!!.moveOriginEndPoint(oldLocation.x, oldLocation.y)
	}
}