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

class ReconnectOriginConnector(
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractReconnectConnector(EdgeViewEndpointType.ORIGIN, connectService, eventBus) {

	/** The [ConnectableView] to which the origin of the [EdgeView] was previously connected. */
	private var origin: ConnectableView? = null

	/** The [Port] to which the origin of the [EdgeView] was previously connected. */
	private var originPort: Port<Any>? = null

	override fun beginDragging(context: EditInputEventContext) {
		super.beginDragging(context)

		origin = edgeView!!.origin
		originPort = edgeView!!.originPort as Port<Any>
		pressLocation = context.location

		connectService.unconnectFromOrigin(edgeView!!)
		val snap = context.editor.snapManager.snap(context.x, context.y)
		edgeView!!.moveOriginEndPoint(context.x + snap.x, context.y + snap.y)
	}

	override fun completeDragOpen(context: EditInputEventContext) {
		completeDragConnecting(context)
	}

	override fun completeDragConnecting(context: EditInputEventContext) {
		val newConnectableView = targetPortView?.owner
		val newPort = targetPortView?.port as Port<Any>?

		if (newConnectableView != null) {
			connectService.connectToOrigin(edgeView!! as EdgeView<Any>, newConnectableView, newPort)
		}

		context.editor.commandManager.beginTransaction(
			command = ReconnectOriginCommand(
				editor = context.editor,
				service = connectService,
				edgeView = edgeView!! as EdgeView<Any>,
				oldConnectableView = origin!!,
				oldPort = originPort!!,
				newPoint = context.location,
				newConnectableView = newConnectableView,
				newPort = newPort
			),
			register = true)

		if (pressLocation.distance(context.x, context.y) < MIN_DRAG_DISTANCE) {
			context.editor.commandManager.rollbackTransaction()
			eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = origin as Component, messageKey = "graph.reconnect.abort.msg"))
		} else {
			context.editor.commandManager.commitTransaction()
		}
	}

	override fun cancel(editor: Editor) {
		connectService.connectToOrigin(edgeView!!, origin!!, originPort)
	}
}

private class ReconnectOriginCommand(
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
		service.unconnectFromOrigin(edgeView)
		if (newConnectableView != null) {
			service.connectToOrigin(edgeView, newConnectableView, newPort)
		} else {
			edgeView.moveOriginEndPoint(newPoint.x, newPoint.y)
		}
	}

	override fun undo() {
		if (newConnectableView != null) {
			service.unconnectFromOrigin(edgeView)
		}
		service.connectToOrigin(edgeView, oldConnectableView, oldPort)
	}
}