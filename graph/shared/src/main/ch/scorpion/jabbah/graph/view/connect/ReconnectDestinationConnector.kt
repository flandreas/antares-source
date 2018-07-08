package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType

/**
 * An [InputEventHandler] that reconnects the destination of a connected [EdgeView] with another [InputPort],
 * or leaves the [EdgeView] open-ended.
 */
class ReconnectDestinationConnector(
        private val connectServiceSupplier: () -> GraphViewConnectService = { GraphViewModule.graphViewConnectService },
        private val eventBus: EventBus = BaseModule.eventBus
) : AbstractDragEdgeViewEndpointConnector(EdgeViewEndpointType.DESTINATION) {

    companion object {
        private val LOG by logger(ReconnectDestinationConnector::class)
	    private const val MIN_DRAG_DISTANCE = 10
    }
    /** The [ConnectableView] to which the destination of the [EdgeView] was previously connected. */
    private var destination: ConnectableView? = null

    /** The [Port] to which the destination of the [EdgeView] was previously connected. */
    private var destinationPort: Port<Any>? = null

	/**
	 * The location where the mouse was pressed. Used to rollback the unconnect action if the user
	 * didn't drag the mouse far enough, assuming that he clicked accidentally.
	 */
    private var pressLocation: Point2D = Point2D()

    /** ---- [InputEventHandler] */

    override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        destination = edgeView!!.destination
        destinationPort = edgeView!!.destinationPort as Port<Any>
	    pressLocation = Point2D(context.x, context.y)

        connectServiceSupplier.invoke().unconnectFromDestination(edgeView!!)

        super.mousePressed(context)

        val snap = context.editor.snapManager.snap(context.x, context.y)
        edgeView!!.moveDestinationEndPoint(context.x + snap.x, context.y + snap.y)

        return this
    }

    override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mouseDragged(context)
        return this
    }

    override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.debug("ReconnectDestinationConnector.mouseReleased at (${context.x},${context.y})")
        super.mouseReleased(context)

        val newConnectableView = getEndpointHandler()!!.targetPortView?.owner
        val newPort = getEndpointHandler()!!.targetPortView?.port as Port<Any>?

        if (newConnectableView != null) {
            connectServiceSupplier.invoke().connectToDestination(edgeView!! as EdgeView<Any>, newConnectableView, newPort)
        }

        context.editor.commandManager.beginTransaction(
                command = ReconnectDestinationCommand(
                        editor = context.editor,
                        service = connectServiceSupplier.invoke(),
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

        return null
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