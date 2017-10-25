package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
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
        private val connectServiceSupplier: () -> GraphViewConnectService = { GraphViewModule.graphViewConnectService }
) : AbstractDragEdgeViewEndpointConnector(EdgeViewEndpointType.ORIGIN) {

    companion object {
        private val LOG by logger(ReconnectOriginConnector::class)
    }

    /** The [ConnectableView] to which the origin of the [EdgeView] was previously connected. */
    private var origin: ConnectableView? = null

    /** The [Port] to which the origin of the [EdgeView] was previously connected. */
    private var originPort: Port<Any>? = null

    /** ---- [InputEventHandler] */

    override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        origin = edgeView!!.origin
        originPort = edgeView!!.originPort as Port<Any>

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
        context.editor.commandManager.commitTransaction()

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