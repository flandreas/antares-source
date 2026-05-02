package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.graph.view.Connection
import io.antarescircuit.jabbah.graph.view.ConnectionReference
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType

class ReconnectOriginConnector(
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractReconnectConnector(EdgeViewEndpointType.ORIGIN, connectService, eventBus) {

	companion object {
		private val LOG by logger(ReconnectOriginConnector::class)
	}

	/** The old origin [Connection] of the [EdgeView]. */
	private var oldOrigin: Connection<Any>? = null

	override fun beginDragging(context: EditInputEventContext) {
		super.beginDragging(context)

		oldOrigin = edgeView!!.origin
		pressLocation = context.location

		connectService.unconnectFromOrigin(edgeView!!)
		val snap = context.editor.snapManager.snap(context.x, context.y)
		edgeView!!.moveOriginEndPoint(context.x + snap.x, context.y + snap.y)
	}

	override fun completeDragOpen(context: EditInputEventContext) {
		completeDragConnecting(context)
	}

	override fun completeDragConnecting(context: EditInputEventContext) {
		LOG.userTrail("Reconnect EdgeView ${edgeView?.id} at port of ${oldOrigin?.connectableView?.id}")

		@Suppress("UNCHECKED_CAST")
		val newConnection = targetPortView?.createConnection() as Connection<Any>?

		if (newConnection != null) {
			connectService.connectToOrigin(edgeView!!, newConnection)
		}

		val originComponentId = (oldOrigin!!.connectableView as Component).id

		context.editor.commandManager.beginTransaction(
			command = ReconnectOriginCommand(
				editor = context.editor,
				service = connectService,
				edgeViewId = edgeView!!.id,
				newPoint = context.location,
				newConnectionRef = newConnection?.asReference
			),
			register = true)

		if (pressLocation.distance(context.x, context.y) < MIN_DRAG_DISTANCE) {
			context.editor.commandManager.rollbackTransaction()
			eventBus.post(ComponentMessage(
				type = ComponentMessageType.Info,
				source = context.editor.drawing.getWithId(originComponentId),
				messageKey = "graph.reconnect.abort.msg"))
		} else {
			context.editor.commandManager.commitTransaction()
		}
	}

	override fun cancel(editor: Editor) {
		connectService.connectToOrigin(edgeView!!, oldOrigin!!)
	}
}

private class ReconnectOriginCommand(
	editor: Editor,
	private val service: GraphViewConnectService,
	private val edgeViewId: Int,
	private val newPoint: Point2D,
	private val newConnectionRef: ConnectionReference?
) : AbstractCommand("graph.command.reconnect", editor) {

	private val edgeView: EdgeView<*> get() = editor!!.drawing.getWithId(edgeViewId) as EdgeView<*>

	override fun getDetailedDescription(): String =
		if (newConnectionRef != null) {
			"${super.getDetailedDescription()} $edgeViewId  to ${newConnectionRef.connectableViewId}:${newConnectionRef.portId}"
		} else {
			"${super.getDetailedDescription()} $edgeViewId open"
		}

	override fun execute() {
		service.unconnectFromOrigin(edgeView)
		if (newConnectionRef != null) {
			service.connectToOrigin(edgeView, newConnectionRef.getConnection(editor!!.drawing as GraphView))
		} else {
			edgeView.moveOriginEndPoint(newPoint.x, newPoint.y)
		}
	}
}