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
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.EdgeView
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

		context.editor.commandManager.beginTransaction(
			command = ReconnectDestinationCommand(
				editor = context.editor,
				service = connectService,
				edgeView = edgeView!!,
				oldConnection = oldDestination!!,
				newPoint = context.location,
				newConnection = newConnection
			),
			register = true)

		if (pressLocation.distance(context.x, context.y) < MIN_DRAG_DISTANCE) {
			context.editor.commandManager.rollbackTransaction()
			eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = oldDestination!!.connectableView as Component, messageKey = "graph.reconnect.abort.msg"))
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
	private val edgeView: EdgeView<Any>,
	private val oldConnection: Connection<Any>,
	private val newPoint: Point2D,
	private val newConnection: Connection<Any>?
) : AbstractCommand("graph.command.reconnect", editor) {

	override fun execute() {
		service.unconnectFromDestination(edgeView)
		if (newConnection != null) {
			service.connectToDestination(edgeView, newConnection)
		} else {
			edgeView.moveDestinationEndPoint(newPoint.x, newPoint.y)
		}
	}

	override fun undo() {
		if (newConnection != null) {
			service.unconnectFromDestination(edgeView)
		}
		service.connectToDestination(edgeView, oldConnection)
	}
}