package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.base.logger

/**
 * Used to drag the origin end point of an [EdgeView] to a new location, or to connect it
 * with an output [PortView] of a [VerticeView].
 */
class DragEdgeViewOriginConnector(
    private val connectServiceSupplier: () -> GraphViewConnectService
) : AbstractDragEdgeViewEndpointConnector(EdgeViewEndpointType.ORIGIN) {

    private val LOG by logger(DragEdgeViewOriginConnector::class)

    /** ---- [InputEventHandler] */

    override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mouseDragged(context)
        return this
    }

    override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.debug("mouseReleased at (${context.x},${context.y})")
        super.mouseReleased(context)

        val moveCmd = MoveOriginEndpointCommand(
            editor = context.editor,
            edgeView = edgeView!!,
            oldLocation = oldLocation,
            newLocation = edgeView!!.originEndpointView.location)

        if (getEndpointHandler()!!.targetPortView == null) {
            context.editor.commandManager.beginTransaction(moveCmd)
        } else {
            val connectCmd = ConnectOriginCommand(
                editor = context.editor,
                    service = connectServiceSupplier.invoke(),
                    edgeView = edgeView!!,
                    origConnectableView = getEndpointHandler()!!.targetPortView!!.owner!!,
                    origPort = getEndpointHandler()!!.targetPortView!!.port)
            context.editor.commandManager.beginTransaction(connectCmd)
            context.editor.commandManager.execute(moveCmd)
        }
        context.editor.commandManager.commitTransaction()
        return null
    }
}