package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A base class for implementing [Drawable]s with a [RectangularShape].
 */
abstract class AbstractRectangle(val shape: RectangularShape) : AbstractDrawable(), RectangularDrawable {

    /** Constructs this [AbstractRectangle] with a [Rectangle2D] shape.*/
    constructor(x: Double, y: Double, w: Double, h: Double) : this(Rectangle2D(x, y, w, h))

    /** Constructs this [AbstractRectangle] with a [Rectangle2D] shape.*/
    constructor(x: Int, y: Int, w: Int, h: Int) : this(Rectangle2D(x, y, w, h))

    /** Constructs this [AbstractRectangle] with an empty [Rectangle2D] shape.*/
    constructor() : this(0.0, 0.0, 0.0, 0.0)

    /** ---- [Locatable] */

    override var location: Point2D
        get() = Point2D(x, y)
        set(value) {
            setBounds(value.x, value.y, width, height)
        }

    /** ---- [AbstractDrawable] */

    /** Calculates the bounding box as the [bounds] extended by the line width on each side of the rectangle.*/
    override val boundingBox: Rectangle2D
        get() {
            return Rectangle2D(
                    bounds.x - lineWidth, bounds.y - lineWidth,
                    bounds.width + 2 * lineWidth, bounds.height + 2 * lineWidth)
        }

    override fun contains(x: Double, y: Double): Boolean {
        return shape.contains(x, y)
    }

    /** ---- [RectangularDrawable] */

    override val bounds: RectangularShape
        get() = shape.boundingBox

    override var width: Double
        get() = bounds.width
        set(value) {
            setBounds(bounds.x, bounds.y, value, bounds.height)
        }

    override var height: Double
        get() = bounds.height
        set(value) {
            setBounds(bounds.x, bounds.y, bounds.width, value)
        }

    override fun setBounds(x: Double, y: Double, w: Double, h: Double) {
        invalidate()
        shape.setFrame(x, y, w, h)
        invalidate()
        update()
    }

    override fun contains(x: Double, y: Double, w: Double, h: Double): Boolean {
        return shape.contains(x, y, w, h)
    }

    /** ---- [AbstractRectangle] */

    /** Draws this [AbstractRectangle]'s [RectangularShape] using the specified [Color]s and [Stroke].*/
    protected fun drawRectangle(context: DrawContext, borderColor: Color?, fillColor: Color?, stroke: Stroke?) {
        drawRectangle(context, shape, borderColor, fillColor, stroke)
    }

    /** Draws the specified [RectangularShape] using the specified [Color]s and [Stroke]. */
    protected fun drawRectangle(context: DrawContext, shape: RectangularShape, borderColor: Color?, fillColor: Color?, stroke: Stroke?) {
        if (fillColor != null) {
            context.g.color = fillColor
            context.g.fill(shape)
        }
        if (borderColor != null && stroke != null) {
            context.g.stroke = stroke
            context.g.color = borderColor
            context.g.draw(shape)
        }
    }

    protected fun drawRectangle(
            context: DrawContext,
            x: Double, y: Double, w: Double, h: Double,
            borderColor: Color?,
            fillColor: Color?,
            stroke: Stroke?
    ) {
        drawRectangle(context, x.toInt(), y.toInt(), w.toInt(), h.toInt(), borderColor, fillColor, stroke)
    }

    protected fun drawRectangle(
            context: DrawContext,
            x: Int, y: Int, w: Int, h: Int,
            borderColor: Color?,
            fillColor: Color?,
            stroke: Stroke?
    ) {
        if (fillColor != null) {
            context.g.color = fillColor
            context.g.fillRect(x, y, w, h)
        }
        if (borderColor != null && stroke != null) {
            context.g.stroke = stroke
            context.g.color = borderColor
            context.g.drawRect(x, y, w, h)
        }
    }
}