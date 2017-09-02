package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Translations
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
            if (componentWrapper != null) {
                (componentWrapper!!.component as OscilloscopeProbeViewComponent).rowNumber = value
            }
        }

    private val handler = Handler()

    private var isHovering: Boolean
        get() = drawable.highlighted
        set(value) {
            drawable.highlighted = value
        }

    private val drawable = OscilloscopeProbeViewDrawable(location, rowNumber, color, styleProvider)

    /**
     * The wrapper of the [OscilloscopeProbeView] to be dragged into the [GraphView].
     * Exists during dragging, and when being contained in the [GraphView]
     */
    private var componentWrapper: GraphElementViewWrapper<GraphElement>? = null

    /** Set to `false` if [componentWrapper] has been dragged into the [GraphView].*/
    private var componentWrapperPresent = true

    private val moveLastLocation = Point2D()

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
        componentWrapperPresent = true
        drawable.filled = true
        componentWrapper = null
        validate()
    }

    /**
     * Handles hovering on this [OscilloscopeProbeView] and dragging of the wrapper of [OscilloscopeProbeViewComponent]
     * into the [GraphView].
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
            if (!componentWrapperPresent) {
                return null
            }
            invalidate()

            drawable.filled = false
            drawable.highlighted = false
            componentWrapperPresent = false
            componentWrapper = GraphElementViewWrapper<GraphElement>(
                    OscilloscopeProbeViewComponent(rowNumber, color, styleProvider),
                    styleProvider
            )
            componentWrapper!!.location = origLocSource.invoke().add(location)
            moveLastLocation.setLocation(context.location)
            context.editor.drawing.add(componentWrapper!!)

            validate()
            return this
        }

        override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            LOG.debug("OscilloscopeProbeView drag ${context.x},${context.y}")
            componentWrapper!!.moveBy(context.x - moveLastLocation.x, context.y - moveLastLocation.y)
            moveLastLocation.setLocation(context.x, context.y)
            return this
        }

        override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            LOG.debug("OscilloscopeProbeView released ${context.x},${context.y}")
            if (!componentWrapperPresent) {
                return null
            }
            invalidate()
            drawable.filled = true
            componentWrapperPresent = true
            context.editor.drawing.remove(componentWrapper!!)
            componentWrapper = null
            validate()
            return null
        }
    }
}