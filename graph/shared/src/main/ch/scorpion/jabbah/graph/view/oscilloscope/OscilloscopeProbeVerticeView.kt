package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.connect.AbstractConnectionPointHighlighter
import ch.scorpion.jabbah.graph.view.port.GenericPortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView

/**
 * The location of this [OscilloscopeProbeVerticeView] as a [Locatable] is the tip of the bubble shape, which is also
 * the connection point.
 * @param T the type of signal that this [OscilloscopeProbeVerticeView]'s [OscilloscopeProbeVertice] can consume.
 */
class OscilloscopeProbeVerticeView<T: Any>(
        rowNumber: Int,
        color: CompositeColor,
        model: OscilloscopeProbeVertice<T>? = OscilloscopeProbeVertice(name = rowNumber.toString()),
        styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangularVerticeView<OscilloscopeProbeVertice<T>>(styleProvider, "graph.component.oscilloscope.port", model) {

    companion object {
        private val LOG by logger(OscilloscopeProbeVerticeView::class)
        private val CONN_POINT_SIZE = 4.0
    }

    init {
        modelExchanged(null)
        setBounds(
                -CONN_POINT_SIZE, -OscilloscopeProbeViewDrawable.SIZE,
                OscilloscopeProbeViewDrawable.SIZE + CONN_POINT_SIZE, OscilloscopeProbeViewDrawable.SIZE + CONN_POINT_SIZE)
    }

    var rowNumber: Int
        get() = drawable.rowNumber
        set(value) {
            invalidate()
            drawable.rowNumber = value
            validate()
        }

    private val drawable = OscilloscopeProbeViewDrawable(Point2D(0.0, -OscilloscopeProbeViewDrawable.SIZE), rowNumber, color, styleProvider)

    /** The [EdgeView] to which this [OscilloscopeProbeVerticeView] is connected.*/
    private var edgeView: EdgeView<T>? = null

    private val handler = Handler()

    private val moveLastLocation = Point2D()

    /** ---- [Component] interface */

    override val type: String? get() = null

    /** ---- [Drawable] interface */

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return handler as InputEventHandler<T>
    }

    /** ---- [AbstractRectangularVerticeView] */

    override fun modelExchanged(oldModel: OscilloscopeProbeVertice<T>?) {
        super.modelExchanged(oldModel)
        addPortView(GenericPortView<T>(model!!.getInput(), 0, 0, Direction.SOUTH))
    }

    override fun drawImpl(context: DrawContext, drawPortViews: Boolean) {
        super.drawImpl(context, drawPortViews)
        drawable.draw(context)
        if (model!!.isConnected) {
            val connPoint = connectionPoint().subtract(location)
            context.g.color = context.choose(drawable.color).foregroundColor
            context.g.fillOval(
                connPoint.x - CONN_POINT_SIZE,
                connPoint.y - CONN_POINT_SIZE,
                2.0 * CONN_POINT_SIZE,
                2.0 * CONN_POINT_SIZE)
        }
    }

    /** ---- [AbstractGraphElementView] */

    override fun handleStateChanged(event: GraphElementEvent) {
        LOG.debug("State of ProbeView changed")
        super.handleStateChanged(event)
    }

    /** ---- [OscilloscopeProbeVerticeView] */

    /** Returns the connection point at the tip of the drop shape in absolute coordinates.*/
    private fun connectionPoint(): Point2D {
        return getPortConnectionPoint(model!!.getPort<Any>())
    }

    private inner class Handler : AbstractConnectionPointHighlighter() {

        override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            LOG.debug("OscilloscopeProbeVerticeView pressed ${context.x},${context.y}")
            moveLastLocation.setLocation(context.location)
            return this
        }

        override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            LOG.debug("OscilloscopeProbeVerticeView dragged ${context.x},${context.y}")

            // Snap
            val dx = context.x - moveLastLocation.x
            val dy = context.y - moveLastLocation.y
            var offset = Point2D()
            if (context.editor.snapManager.snapEnabled) {
                offset = context.editor.snapManager.snap(this@OscilloscopeProbeVerticeView, dx, dy)
            }

            // Perform drag
            moveBy(dx + offset.x, dy + offset.y)
            moveLastLocation.setLocation(context.x + offset.x, context.y + offset.y)
            validate()

            // Sensing EdgeView
            if (findEdgeView(context) != null) {
                LOG.debug("OscilloscopeProbeVerticeView found EdgeView at ${context.x},${context.y}")
                displayPortViewHighlight(context.drawingView(), connectionPoint())
            } else {
                removePortViewHighlight(context.drawingView())
            }

            return this
        }

        override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            LOG.debug("OscilloscopeProbeVerticeView released ${context.x},${context.y}")
            removePortViewHighlight(context.drawingView())

            // TODO Create Commands

            invalidate()
            val newEdgeView = findEdgeView(context) as EdgeView<T>?
            if (edgeView != null && edgeView !== newEdgeView) {
                edgeView!!.model!!.unconnect(model!!.getPort<T>())
            }
            if (newEdgeView != null && newEdgeView !== edgeView) {
                newEdgeView.model!!.connect(model!!.getPort<T>())
            }
            edgeView = newEdgeView

            invalidate()
            validate()

            return null
        }

        private fun findEdgeView(context: EditInputEventContext): EdgeView<*>? {
            return context.drawingView().drawing.getDrawable { it.contains(connectionPoint()) && it is EdgeView<*> } as EdgeView<*>?
        }
    }
}