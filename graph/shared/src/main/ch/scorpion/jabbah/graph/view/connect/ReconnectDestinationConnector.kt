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

class ReconnectDestinationConnector(
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractReconnectConnector(EdgeViewEndpointType.DESTINATION, connectService, eventBus) {

	/** The old destination [Connection] of the [EdgeView]. */
	private var oldDestination: Connection<Any>? = null

	override fun beginDragging(context: EditInputEventContext) {
		super.beginDragging(context)

		oldDestination = edgeView!!.destination
		pressLocation = context.location

		connectService.unconnectFromDestination(edgeView!!)
		val snap = context.editor.snapManager.snap(context.x, context.y)
		edgeView!!.moveDestinationEndPoint(context.x + snap.x, context.y + snap.y)
	}

	override fun completeDragOpen(context: EditInputEventContext) {
		completeDragConnecting(context)
	}

	override fun completeDragConnecting(context: EditInputEventContext) {
		val newConnection = targetPortView?.createConnection() as Connection<Any>?

		if (newConnection != null) {
			connectService.connectToDestination(edgeView!!, newConnection)
		}

		val destinationComponentId = (oldDestination!!.connectableView as Component).id

		context.editor.commandManager.beginTransaction(
			command = ReconnectDestinationCommand(
				editor = context.editor,
				service = connectService,
				edgeViewId = edgeView!!.id,
				oldConnection = oldDestination!!,
				newPoint = context.location,
				newConnectionRef = newConnection?.asReference
			),
			register = true)

		if (pressLocation.distance(context.x, context.y) < MIN_DRAG_DISTANCE) {
			context.editor.commandManager.rollbackTransaction()
			eventBus.post(ComponentMessage(
				type = ComponentMessageType.Info,
				source = context.editor.drawing.getWithId(destinationComponentId),
				messageKey = "graph.reconnect.abort.msg"))
		} else {
			context.editor.commandManager.commitTransaction()
		}
	}

	override fun cancel(editor: Editor) {
		connectService.connectToDestination(edgeView!!, oldDestination!!)
	}
}

private class ReconnectDestinationCommand(
	editor: Editor,
	private val service: GraphViewConnectService,
	private val edgeViewId: Int,
	private val oldConnection: Connection<Any>,
	private val newPoint: Point2D,
	private val newConnectionRef: ConnectionReference?
) : AbstractCommand("graph.command.reconnect", editor) {

	private val edgeView: EdgeView<*> get() = editor!!.drawing.getWithId(edgeViewId) as EdgeView<*>

	override fun execute() {
		service.unconnectFromDestination(edgeView)
		if (newConnectionRef != null) {
			service.connectToDestination(edgeView, newConnectionRef.getConnection(editor!!.drawing as GraphView))
		} else {
			edgeView.moveDestinationEndPoint(newPoint.x, newPoint.y)
		}
	}
}