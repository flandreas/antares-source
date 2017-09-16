package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * An [InputEventHandler] that splits an existing [EdgeView] by adding a [NodeView] and connecting it
 * by a new [EdgeView] with the [PortView] of another [VerticeView].
 */
class EdgeToPortConnector(
    private val connectServiceSupplier: () -> GraphViewConnectService,
    edgeViewFactorySupplier: () -> EdgeViewFactory<Any>
) : AbstractConnector(edgeViewFactorySupplier, DragEdgeViewEndpointHandler(EdgeViewEndpointType.DESTINATION)) {

    companion object {
        private val EDGE_CORNER_DIST = 15
    }

    private val LOG by logger(EdgeToPortConnector::class)

    /** The [EdgeView] from which new [EdgeView]s are branched by this connector. */
    private var branchedEdgeView: EdgeView<*>? = null

    /** The index of the [EdgeView] segment at which splitting takes place.*/
    private var branchedSegmentIndex: Int? = null

    /** The [NodeView] being created when splitting.*/
    private var nodeView: NodeView<*>? = null

    private var splitResult: SplitEdgeViewResult<*>? = null

    /** ---- [InputEventHandler] */

    private data class SnapResult(val segmentIndex: Int, val x: Double, val y: Double)

    private fun snap(context: EditInputEventContext): SnapResult? {
        val segmentIndex = branchedEdgeView!!.findSegment(context.x, context.y) ?: return null
        var x = context.x
        var y = context.y

        // Try to snap to a nearby [EdgeView] corner, if any
        if (branchedEdgeView!!.getSegmentPoint(segmentIndex).distance(context.x, context.y) <= EDGE_CORNER_DIST) {
            x = branchedEdgeView!!.getSegmentPoint(segmentIndex).x
            y = branchedEdgeView!!.getSegmentPoint(segmentIndex).y
        } else if (segmentIndex < branchedEdgeView!!.segmentPointCount - 2 && branchedEdgeView!!.getSegmentPoint(segmentIndex + 1).distance(context.x, context.y) <= EDGE_CORNER_DIST) {
            x = branchedEdgeView!!.getSegmentPoint(segmentIndex + 1).x
            y = branchedEdgeView!!.getSegmentPoint(segmentIndex + 1).y
        }

        // Additionally, snap to the grid
        val snap = context.editor.snapManager.snap(x, y)

        return SnapResult(segmentIndex, x + snap.x, y + snap.y)
    }

    override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.trace("mouseMoved to (${context.x},${context.y})")

        val snapResult = snap(context)
        if (snapResult != null) {
            displayPortViewHighlight(context.drawingView(), Point2D(snapResult.x, snapResult.y))
            return this
        }

        if (portViewHighlight != null) {
            removePortViewHighlight(context.drawingView())
        }
        return null
    }

    override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.trace("mousePressed at (${context.x},${context.y})")

        val snapResult = snap(context)
        if (snapResult == null) {
            return null
        }
        branchedSegmentIndex = snapResult.segmentIndex

        beginConnecting(context.drawingView())
        return this
    }

    override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.trace("mouseDragged to (${context.x},${context.y})")
        // Forward to DragEdgeViewEndpointHandler, but keep control in order to handle mouseReleased by returning this
        super.mouseDragged(context)
        return this
    }

    override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mouseReleased(context)
        if (isValidEdgeView()) {
            completeConnecting(context)
        } else {
            cancel(context.drawingView())
        }
        return null
    }

    /** ---- [EdgeToPortConnector] */

    fun useFor(edgeView: EdgeView<*>) {
        branchedEdgeView = edgeView
    }

    private fun getEndpointHandler(): DragEdgeViewEndpointHandler {
        return successor as DragEdgeViewEndpointHandler
    }

    private fun beginConnecting(view: DrawingView<Drawing<Component>>) {
        createEdgeView(view, Point2D(portViewHighlight!!.location), branchedEdgeView!!.model as Net<Any>)
        getEndpointHandler().useFor(edgeView!!)
        removePortViewHighlight(view)

        splitResult = connectServiceSupplier.invoke().split(
            view.drawing as GraphView<GraphElementView<*>>,
            branchedEdgeView!! as EdgeView<Any>,
            branchedSegmentIndex!!,
            edgeView!!,
            null)
    }

    private fun completeConnecting(context: EditInputEventContext) {
        if (getEndpointHandler().targetPortView != null) {
            connectServiceSupplier.invoke().connectToDestination(
                    edgeView!!,
                    getEndpointHandler().targetPortView!!.owner!!,
                    getEndpointHandler().targetPortView!!.port as Port<Any>)
        }

        val splitCmd = SplitEdgeViewCommand(
                editor = context.editor,
                connectService = connectServiceSupplier.invoke(),
                graphView = context.drawingView().drawing as GraphView<GraphElementView<*>>,
                origEdgeView = branchedEdgeView!!,
                segmentIndex = branchedSegmentIndex!!,
                newEdgeView = edgeView!!,
                targetPortView = getEndpointHandler().targetPortView,
                nodeView = splitResult!!.nodeView)

        context.editor.commandManager.register(splitCmd)
    }
}