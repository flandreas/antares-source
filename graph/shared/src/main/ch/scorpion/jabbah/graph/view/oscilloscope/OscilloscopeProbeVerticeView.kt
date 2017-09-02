package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbe
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.connect.AbstractConnectionPointHighlighter
import ch.scorpion.jabbah.graph.view.port.GenericPortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView

/**
 * The location of this [OscilloscopeProbeVerticeView] as a [Locatable] is the tip of the bubble shape, which is also
 * the connection point.
 * @param T the type of signal that this [OscilloscopeProbeVerticeView]'s [OscilloscopeProbe] can consume.
 */
class OscilloscopeProbeVerticeView<T: Any>(
        rowNumber: Int,
        color: CompositeColor,
        model: OscilloscopeProbe<T>? = OscilloscopeProbe(),
        styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangularVerticeView<OscilloscopeProbe<T>>(styleProvider, "graph.component.oscilloscope.port", model) {

    companion object {
        private val LOG by logger(OscilloscopeProbeVerticeView::class)
    }

    init {
        modelExchanged(null)
        setBounds(0.0, -OscilloscopeProbeViewDrawable.SIZE, OscilloscopeProbeViewDrawable.SIZE, OscilloscopeProbeViewDrawable.SIZE)
    }

    var rowNumber: Int
        get() = drawable.rowNumber
        set(value) {
            invalidate()
            drawable.rowNumber = value
            validate()
        }

    private val drawable = OscilloscopeProbeViewDrawable(Point2D(0.0, -OscilloscopeProbeViewDrawable.SIZE), rowNumber, color, styleProvider)

    private val handler = Handler()

    private val moveLastLocation = Point2D()

    /** ---- [Component] interface */

    override val type: String? get() = null

    /** ---- [Drawable] interface */

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return handler as InputEventHandler<T>
    }

    /** ---- [AbstractRectangularVerticeView] */

    override fun modelExchanged(oldModel: OscilloscopeProbe<T>?) {
        super.modelExchanged(oldModel)
        addPortView(GenericPortView<T>(model!!.getInput(), 0, 0, Direction.SOUTH))
    }

    override fun drawImpl(context: DrawContext, drawPortViews: Boolean) {
        super.drawImpl(context, drawPortViews)
        drawable.draw(context)
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
            val connPoint = getPortConnectionPoint(model!!.getPort<Any>())
            val edgeView = context.drawingView().drawing.getDrawable { it.contains(connPoint) && it is EdgeView<*> }
            if (edgeView != null) {
                LOG.debug("OscilloscopeProbeVerticeView found EdgeView at ${context.x},${context.y}")
                displayPortViewHighlight(context.drawingView(), connPoint)
            } else {
                removePortViewHighlight(context.drawingView())
            }

            return this
        }

        override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            LOG.debug("OscilloscopeProbeVerticeView released ${context.x},${context.y}")
            removePortViewHighlight(context.drawingView())
            // TODO Create Command
            return null
        }
    }
}