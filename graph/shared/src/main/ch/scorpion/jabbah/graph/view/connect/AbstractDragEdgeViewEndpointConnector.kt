package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeEndpointView
import ch.scorpion.jabbah.draw.graphics.Cursor


/**
 * Abstract base implementation of an [InputEventHandler] that supports dragging
 * an [EdgeView] endpoint to a new location, or to connect the [EdgeView] with
 * an input or output [PortView] of a [VerticeView].
 */
abstract class AbstractDragEdgeViewEndpointConnector(
    private val endpointType: EdgeViewEndpointType
) : AbstractConnectionPointHighlighter(DragEdgeViewEndpointHandler(endpointType)) {

    companion object {
        private val LOG by logger(AbstractDragEdgeViewEndpointConnector::class)
    }

    /** The [EdgeView] whose endpoint is being dragged. */
    protected var edgeView: EdgeView<*>? = null

    /** The location where dragging started. */
    protected var oldLocation = Point2D.ZERO

    /** ---- [InputEventHandler] */

    override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.trace("AbstractDragEdgeViewEndpointConnector.mouseMoved")
        if (endpointType.getEndpoint(edgeView!!).contains(context.x, context.y)) {
            displayPortViewHighlight(context.drawingView(), getEndpointView().location)
            return this
        }

        if (portViewHighlight != null) {
            context.view.setCursor(Cursor.DEFAULT)
            removePortViewHighlight(context.drawingView())
            return null
        }

        return null
    }

    override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.debug("AbstractDragEdgeViewEndpointConnector.mousePressed")
        removePortViewHighlight(context.drawingView())
        oldLocation = getEndpointView().location
        context.drawingView().selectionManager.deselectAll()
        context.drawingView().selectionManager.select(edgeView!!)

        getEndpointHandler()!!.useFor(edgeView!!)
        getEndpointHandler()!!.mousePressed(context)
        return this
    }

    /** ---- [AbstractDragEdgeViewEndpointConnector] */

    fun useFor(edgeView: EdgeView<*>) {
        this.edgeView = edgeView
    }

    protected fun getEndpointHandler(): DragEdgeViewEndpointHandler? {
        return successor as DragEdgeViewEndpointHandler?
    }

    protected fun getEndpointType(): EdgeViewEndpointType? {
        return getEndpointHandler()!!.edgeViewEndpointType
    }

    protected fun getEndpointView(): EdgeEndpointView {
        return getEndpointType()!!.getEndpoint(edgeView!!)
    }
}