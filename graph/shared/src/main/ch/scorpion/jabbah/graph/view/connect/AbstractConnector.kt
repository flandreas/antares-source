package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory

/**
 * Abstract base implementation of an [InputEventHandler] that creates a new [EdgeView]
 * to connect [ConnectableView]s.
 */
abstract class AbstractConnector(
    private val edgeViewFactorySupplier: () -> EdgeViewFactory<Any>,
    successor: InputEventHandler<EditInputEventContext>?
) : AbstractConnectionPointHighlighter(successor) {

    /** The new [EdgeView] that is being dragged, `null` before mouse has been pressed */
    protected var edgeView: EdgeView<Any>? = null

    /** ---- [InputEventHandler] */

    override fun keyPressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        return this
    }

    override fun keyReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        return this
    }

    /** ---- [AbstractConnector] */

    /**
     * Creates the [EdgeView] to be used for connecting and adds it to the [Drawing].
     * Removes the [PortView] highlight.
     *
     * @param view the [DrawingView] to which the created [EdgeView] is added
     * @param startPoint the [Point2D] at which the created [EdgeView] starts.
     * @param net the [Net] model of the [EdgeView] to be created
     */
    protected fun createEdgeView(view: DrawingView<Drawing<Component>>, startPoint: Point2D, net: Net<Any>?) {
        edgeView = if (net == null) edgeViewFactorySupplier.invoke().createEdgeView() else edgeViewFactorySupplier.invoke().createEdgeView(net)

        // Add the connection point twice so that the second point can be dragged
        edgeView!!.addSegmentPoint(startPoint)
        edgeView!!.addSegmentPoint(startPoint)

        view.drawing.add(edgeView!!)
        view.selectionManager.deselectAll()
        view.selectionManager.select(edgeView!!)
    }

    /*
    protected open fun cancel(view: DrawingView<Drawing<Component>>) {
        view.drawing.remove(edgeView!!)
        removePortViewHighlight(view)
    }
    */
    protected open fun cancel(editor: Editor) {
        editor.view.drawing.remove(edgeView!!)
        removePortViewHighlight(editor.view)
    }

    /**
     * Determines whether the [EdgeView] that is being added by this [AbstractConnector]
     * is valid, i.e. that it exists and has a non-zero length.
     */
    protected fun isValidEdgeView(): Boolean {
        return edgeView != null && edgeView!!.length > 0.0
    }
}