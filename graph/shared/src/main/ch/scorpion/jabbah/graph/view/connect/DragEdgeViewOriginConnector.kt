package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType

class DragEdgeViewOriginConnector(
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService
) : AbstractDragEdgeViewEndpointConnector(connectService, EdgeViewEndpointType.ORIGIN) {

	companion object {
		private val LOG by logger(DragEdgeViewOriginConnector::class)
	}

	override fun completeDragOpen(context: EditInputEventContext) {
		LOG.userTrail("Move EdgeView ${edgeView!!.id} endpoint open-ended")
		context.editor.commandManager.beginTransaction(createMoveCommand(context))
		context.editor.commandManager.commitTransaction()
	}

	override fun completeDragConnecting(context: EditInputEventContext) {
		LOG.userTrail("Move EdgeView endpoint to connect port ${targetPortView!!.port.portId} of ${targetPortView?.owner?.id}")
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