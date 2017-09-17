package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.base.logger


/**
 * An [InputEventHandler] that connects an [OutputPort] of a [VerticeView] with an [InputPort]
 * of a [VerticeView], or leaves the [EdgeView] open-ended.
 */
class OutputToInputConnector(
    private val connectServiceSupplier: () -> GraphViewConnectService,
    edgeViewFactorySupplier: () -> EdgeViewFactory<Any>
) : AbstractConnector(edgeViewFactorySupplier, DragEdgeViewEndpointHandler(EdgeViewEndpointType.DESTINATION)) {

    private val LOG by logger(OutputToInputConnector::class)

    /** The [VerticeView] from which the new connection originates. */
    private var verticeView: VerticeView<*>? = null

    /** The [PortView] in [verticeView] from which the new connection originates.  */
    private var origPortView: PortView<*>? = null

    fun useFor(verticeView: VerticeView<*>) {
        this.verticeView = verticeView
    }

    /** ---- [InputEventHandler] */

    override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        if (verticeView!!.contains(context.x, context.y)) {
            val pv = verticeView!!.getPortViewAt(context.x, context.y)

            if (pv != null && !pv.port.isConnected && pv.port.portType.isOutput) {

                origPortView = pv
                if (portViewHighlight == null) {
                    val connPoint = verticeView!!.getPortConnectionPoint(origPortView!!.port)
                    displayPortViewHighlight(context.drawingView(), Point2D(connPoint))
                }
                return this
            }
        }
        if (portViewHighlight != null) {
            removePortViewHighlight(context.drawingView())
            origPortView = null
        }
        return null
    }

    override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        if (portViewHighlight == null) {
            return null
        }

        createEdgeView(context.drawingView(), verticeView!!.getPortConnectionPoint(origPortView!!.port), null)
        getEndpointHandler().useFor(edgeView!!)
        removePortViewHighlight(context.drawingView())

        edgeView!!.model!!.connect(origPortView!!.port as Port<Any>)
        edgeView!!.connectToOrigin(origPortView!!.owner, origPortView!!.port as Port<Any>)

        return this
    }

    override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.trace("mouseDragged to (${context.x},${context.y})")
        // Forward to DragEdgeViewEndpointHandler, but keep control in order to handle mouseReleased
        super.mouseDragged(context)
        return this
    }

    override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.debug("mouseReleased at (${context.x},${context.y})")
        super.mouseReleased(context)

        if (isValidEdgeView()) {
            completeConnecting(context)
        } else {
            cancel(context.editor)
        }

        return null
    }

    /** --- [OutputToInputConnector] */

    private fun getEndpointHandler(): DragEdgeViewEndpointHandler {
        return successor as DragEdgeViewEndpointHandler
    }

    private fun completeConnecting(context: EditInputEventContext) {
        context.drawingView().drawing.remove(edgeView!!)

        val targetPortView = getEndpointHandler().targetPortView
        context.editor.commandManager.execute(
                ConnectCommand(
                        editor = context.editor,
                        connectService = connectServiceSupplier.invoke(),
                        edgeView = edgeView!!,
                        origConnectableView = origPortView!!.owner,
                        origPort = origPortView!!.port,
                        destConnectableView = targetPortView?.owner,
                        destPort = targetPortView?.port))
        context.drawingView().selectionManager.select(edgeView!!)
    }
}