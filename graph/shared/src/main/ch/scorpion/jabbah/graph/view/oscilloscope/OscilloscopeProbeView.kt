package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphElementViewWrapper

/** The probe view that is contained in a row of a [OscilloscopeView].*/
class OscilloscopeProbeView(
        location: Point2D,
        rowNumber: Int,
        private val color: CompositeColor,
        private val origLocSource: () -> Point2D,
        private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangle(location.x, location.y, OscilloscopeProbeViewDrawable.SIZE, OscilloscopeProbeViewDrawable.SIZE) {

    companion object {
        private val LOG by logger(OscilloscopeProbeView::class)
    }

    var rowNumber: Int
        get() = drawable.rowNumber
        set(value) {
            drawable.rowNumber = value
            if (vertice != null) {
                vertice!!.rowNumber = value
            }
        }

    /**
     * The [OscilloscopeProbeVerticeView] to be dragged into the [GraphView].
     * Exists during dragging, and when being contained in the [GraphView].
     * Can be set by [OscilloscopeView] while reading from persistent store.
     */
    var vertice: OscilloscopeProbeVerticeView<Any>? = null
        set(value) {
            if (field !== value) {
                field = value
                verticePresent = false
                drawable.filled = false
            }
        }


    private val handler = Handler()

    private var isHovering: Boolean
        get() = drawable.highlighted
        set(value) {
            drawable.highlighted = value
        }

    private val drawable = OscilloscopeProbeViewDrawable(location, rowNumber, color, styleProvider)

    /** Set to `false` if [vertice] has been dragged into the [GraphView].*/
    private var verticePresent = true

    /** ---- [Drawable] */

    override fun draw(context: DrawContext) {
        drawable.draw(context)
    }

    override val lineWidth: Double get() = drawable.lineWidth

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return handler as InputEventHandler<T>
    }

    override fun getToolTipText(x: Double, y: Double, width: Int?): String? {
        return Translations.getString("graph.action.oscilloscope.dragProbe.name")
    }

    /** ---- [OscilloscopeProbeView] */

    fun handleProbeViewRemovedFromDrawing() {
        invalidate()
        verticePresent = true
        drawable.filled = true
        vertice = null
        validate()
    }

    /**
     * Handles hovering on this [OscilloscopeProbeView] and delegates to the [InputEventHandler] of
     * its [OscilloscopeProbeVerticeView] to control dragging into the [GraphView].
     */
    private inner class Handler : InputEventHandlerAdapter<EditInputEventContext>() {

        override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            LOG.debug("OscilloscopeProbeView mouseMoved ${context.x},${context.y}")
            // TODO Refactoring: Copy/Paste from IconButton. Extract hovering behaviour
            if (contains(context.x, context.y)) {
                if (!isHovering) {
                    isHovering = true
                    invalidate()
                    validate()
                }
                return this
            }
            if (isHovering) {
                isHovering = false
                invalidate()
                validate()
            }
            return null
        }

        override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            LOG.debug("OscilloscopeProbeView pressed ${context.x},${context.y}")
            if (!verticePresent) {
                return null
            }
            invalidate()

            drawable.filled = false
            drawable.highlighted = false
            verticePresent = false

            vertice = OscilloscopeProbeVerticeView(rowNumber = rowNumber, color = color, styleProvider = styleProvider)
            vertice!!.location = origLocSource.invoke().add(location).add(Point2D(0.0, height))
            context.editor.drawing.add(vertice!!)

            validate()
            return vertice!!.getInputEventHandler(context).mousePressed(context)
        }
    }
}