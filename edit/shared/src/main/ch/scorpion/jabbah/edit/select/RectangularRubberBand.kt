package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.select.RubberBand.Companion.PROP_FILL_PAINT
import ch.scorpion.jabbah.edit.select.RubberBand.Companion.PROP_STROKE
import ch.scorpion.jabbah.edit.select.RubberBand.Companion.PROP_STROKE_PAINT
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * A [RubberBand] of rectangular shape.
 */
class RectangularRubberBand : AbstractRectangle(Rectangle2D()), RubberBand {

    val LOG by logger(RectangularRubberBand::class)

    /** ---- [Unzoomable] interface */

    override var zoomPan: ZoomPan? = null

    /** ---- [Rubberband] interface */

    override val inputEventHandler: InputEventHandler<EditInputEventContext> = EventHandler()

    /** ---- [Drawable] */

    override fun draw(context: DrawContext) {
        if (zoomPan != null) {
            val p1 = zoomPan!!.transform.modelToView(Point2D(x, y))
            val p2 = zoomPan!!.transform.modelToView(Point2D(x + width, y + height))
            drawRectangle(
                context,
                p1.x, p1.y, p2.x - p1.x, p2.y - p1.y,
                DrawModule.properties.getColor(PROP_STROKE_PAINT),
                DrawModule.properties.getColor(PROP_FILL_PAINT),
                DrawModule.properties.getStroke(PROP_STROKE)
            )
        }
    }

    /** ---- [RectangularDrawable] */

    override val lineWidth: Double
        get() = DrawModule.properties.getStroke(PROP_STROKE).width.toDouble()

    /** ---- [RectangularRubberBand] */

    private inner class EventHandler : InputEventHandlerAdapter<EditInputEventContext>() {
        private var pressedX: Double = 0.0
        private var pressedY: Double = 0.0

        override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            super.mousePressed(context)
            zoomPan = context.view.zoomPan
            pressedX = context.x
            pressedY = context.y
            // Add RubberBand before setting bounds, because otherwise the zoom factor is not yet set,
            // which will result in an infinite bounding box
            context.drawingView().ghostContainer.add(this@RectangularRubberBand)
            setBounds(pressedX, pressedY, 0.0, 0.0)
            return this
        }

        override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            super.mouseDragged(context)
            setBounds(
                Math.min(pressedX, context.x),
                Math.min(pressedY, context.y),
                Math.abs(pressedX - context.x),
                Math.abs(pressedY - context.y)
            )
            validate()
            return this
        }

        override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            LOG.debug("RectangularRubberBand: mouseReleased")
            super.mouseReleased(context)
            context.drawingView().ghostContainer.remove(this@RectangularRubberBand)
            context.drawingView().ghostContainer.validate()
            return this
        }
    }
}