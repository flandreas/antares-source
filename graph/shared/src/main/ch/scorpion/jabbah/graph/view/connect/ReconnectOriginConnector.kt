package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.ConnectionReference
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType

class ReconnectOriginConnector(
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractReconnectConnector(EdgeViewEndpointType.ORIGIN, connectService, eventBus) {

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

	override fun execute() {
		service.unconnectFromOrigin(edgeView)
		if (newConnectionRef != null) {
			service.connectToOrigin(edgeView, newConnectionRef.getConnection(editor!!.drawing as GraphView))
		} else {
			edgeView.moveOriginEndPoint(newPoint.x, newPoint.y)
		}
	}
}