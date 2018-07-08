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
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType

/**
 * An [InputEventHandler] that reconnects the origin of a connected [EdgeView] with another [OutputPort],
 * or leaves the [EdgeView] open-beginning.
 */
class ReconnectOriginConnector(
        private val connectServiceSupplier: () -> GraphViewConnectService = { GraphViewModule.graphViewConnectService },
        private val eventBus: EventBus = BaseModule.eventBus
) : AbstractDragEdgeViewEndpointConnector(EdgeViewEndpointType.ORIGIN) {

    companion object {
        private val LOG by logger(ReconnectOriginConnector::class)
	    private const val MIN_DRAG_DISTANCE = 10
    }

    /** The [ConnectableView] to which the origin of the [EdgeView] was previously connected. */
    private var origin: ConnectableView? = null

    /** The [Port] to which the origin of the [EdgeView] was previously connected. */
    private var originPort: Port<Any>? = null

    /**
     * The location where the mouse was pressed. Used to rollback the unconnect action if the user
     * didn't drag the mouse far enough, assuming that he clicked accidentally.
     */
    private var pressLocation: Point2D = Point2D()

    /** ---- [InputEventHandler] */

    override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        origin = edgeView!!.origin
        originPort = edgeView!!.originPort as Port<Any>
	    pressLocation = Point2D(context.x, context.y)

        connectServiceSupplier.invoke().unconnectFromOrigin(edgeView!!)

        super.mousePressed(context)

        val snap = context.editor.snapManager.snap(context.x, context.y)
        edgeView!!.moveOriginEndPoint(context.x + snap.x, context.y + snap.y)

        return this
    }

    override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mouseDragged(context)
        return this
    }

    override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.debug("ReconnectOriginConnector.mouseReleased at (${context.x},${context.y})")
        super.mouseReleased(context)

        val newConnectableView = getEndpointHandler()!!.targetPortView?.owner
        val newPort = getEndpointHandler()!!.targetPortView?.port as Port<Any>?

        if (newConnectableView != null) {
            connectServiceSupplier.invoke().connectToOrigin(edgeView!! as EdgeView<Any>, newConnectableView, newPort)
        }

        context.editor.commandManager.beginTransaction(
                command = ReconnectOriginCommand(
                        editor = context.editor,
                        service = connectServiceSupplier.invoke(),
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

        return null
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