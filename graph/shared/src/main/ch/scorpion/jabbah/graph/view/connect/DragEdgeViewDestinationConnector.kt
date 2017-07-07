package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.base.logger

/**
 * Used to drag the destination end point of an [EdgeView] to a new location, or to connect it
 * with an input [PortView] of a [VerticeView].
 */
class DragEdgeViewDestinationConnector(
        private val connectServiceSupplier: () -> GraphViewConnectService
) : AbstractDragEdgeViewEndpointConnector(EdgeViewEndpointType.DESTINATION) {

    private val LOG by logger()

    /** ---- [InputEventHandler] */

    override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mouseDragged(context)
        return this
    }

    override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.debug("mouseReleased at (${context.x},${context.y})")
        super.mouseReleased(context)

        val moveCmd = MoveDestinationEndpointCommand(
                editor = context.editor,
                edgeView = edgeView!!,
                oldLocation = oldLocation,
                newLocation = edgeView!!.destinationEndpointView.location)

        if (getEndpointHandler()!!.targetPortView == null) {
            context.editor.commandManager.beginTransaction(moveCmd)
        } else {
            val connectCmd = ConnectDestinationCommand(
                    editor = context.editor,
                    service = connectServiceSupplier.invoke(),
                    edgeView = edgeView!!,
                    destConnectableView = getEndpointHandler()!!.targetPortView!!.owner!!,
                    destPort = getEndpointHandler()!!.targetPortView!!.port)
            context.editor.commandManager.beginTransaction(connectCmd)
            context.editor.commandManager.execute(moveCmd)
        }
        context.editor.commandManager.commitTransaction()
        return null
    }
}