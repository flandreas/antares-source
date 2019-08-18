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
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType

class ReconnectDestinationConnector(
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractReconnectConnector(EdgeViewEndpointType.DESTINATION, connectService, eventBus) {

	/** The [ConnectableView] to which the destination of the [EdgeView] was previously connected. */
	private var destination: ConnectableView? = null

	/** The [Port] to which the destination of the [EdgeView] was previously connected. */
	private var destinationPort: Port<Any>? = null

	override fun beginDragging(context: EditInputEventContext) {
		super.beginDragging(context)

		destination = edgeView!!.destination
		destinationPort = edgeView!!.destinationPort as Port<Any>
		pressLocation = context.location

		connectService.unconnectFromDestination(edgeView!!)
		val snap = context.editor.snapManager.snap(context.x, context.y)
		edgeView!!.moveDestinationEndPoint(context.x + snap.x, context.y + snap.y)
	}

	override fun completeDragOpen(context: EditInputEventContext) {
		completeDragConnecting(context)
	}

	override fun completeDragConnecting(context: EditInputEventContext) {
		val newConnectableView = targetPortView?.owner
		val newPort = targetPortView?.port as Port<Any>?

		if (newConnectableView != null) {
			connectService.connectToDestination(edgeView!! as EdgeView<Any>, newConnectableView, newPort)
		}

		context.editor.commandManager.beginTransaction(
			command = ReconnectDestinationCommand(
				editor = context.editor,
				service = connectService,
				edgeView = edgeView!! as EdgeView<Any>,
				oldConnectableView = destination!!,
				oldPort = destinationPort!!,
				newPoint = context.location,
				newConnectableView = newConnectableView,
				newPort = newPort
			),
			register = true)

		if (pressLocation.distance(context.x, context.y) < MIN_DRAG_DISTANCE) {
			context.editor.commandManager.rollbackTransaction()
			eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = destination as Component, messageKey = "graph.reconnect.abort.msg"))
		} else {
			context.editor.commandManager.commitTransaction()
		}
	}

	override fun cancel(editor: Editor) {
		connectService.connectToDestination(edgeView!!, destination!!, destinationPort)
	}
}

private class ReconnectDestinationCommand(
	editor: Editor,
	private val service: GraphViewConnectService,
	private val edgeView: EdgeView<Any>,
	private val oldConnectableView: ConnectableView,
	private val oldPort: Port<Any>,
	private val newPoint: Point2D,
	private val newConnectableView: ConnectableView?,
	private val newPort: Port<Any>?
) : AbstractCommand("graph.command.reconnect", editor) {

	override fun execute() {
		service.unconnectFromDestination(edgeView)
		if (newConnectableView != null) {
			service.connectToDestination(edgeView, newConnectableView, newPort)
		} else {
			edgeView.moveDestinationEndPoint(newPoint.x, newPoint.y)
		}
	}

	override fun undo() {
		if (newConnectableView != null) {
			service.unconnectFromDestination(edgeView)
		}
		service.connectToDestination(edgeView, oldConnectableView, oldPort)
	}
}