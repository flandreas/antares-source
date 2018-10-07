package ch.scorpion.jabbah.draw.polyline

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractStyledDrawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

/**
 * A standard [Drawable] implementation of a [Polyline].
 */
class PolylineDrawable constructor(
    val shape: PolylineShape = DrawModule.polylineShapeFactory.invoke(null),
    styleType: StyleType = StyleType.FIGURE,
    styleProvider: StyleProvider = DrawStyleModule.styleProvider
): AbstractStyledDrawable(styleType, styleProvider), Polyline, Locatable, Transparent {

	/** ---- [Any] */

    override fun toString(): String {
        return Translations.getString("edit.component.polyline")
    }

    /** ---- [Locatable] interface */

    override var location: Point2D
        get() = Point2D(shape.getPointAt(0))
        set(value) {
            invalidate()
            shape.setLocation(value.x, value.y)
            invalidate()
            update()
        }

    override fun moveBy(dx: Double, dy: Double) {
        invalidate()
        shape.setLocation(location.x + dx, location.y + dy)
        invalidate()
        update()
    }

    /** ---- [Transparent] interface */

    private val transparent = TransparentImpl(this)

    override var transparency: Int
        get() = transparent.transparency
        set(value) { transparent.transparency = value }

    /** ---- [Drawable] interface */

    override val boundingBox: RectangularShape
        get() {
            val bbox = shape.boundingBox
            val lw = getLineWidth()
            bbox.setFrame(bbox.x - lw, bbox.y - lw, bbox.width + 2 * lw, bbox.height + 2 * lw)
            return bbox
        }

    override fun draw(context: DrawContext) {
        if (context.useContextColors) {
            drawImpl(context, context.color!!.foregroundColor, context.color!!.backgroundColor)
        } else {
            drawImpl(context, transparent.applyTo(foregroundColor), if (filled) transparent.applyTo(backgroundColor) else null)
        }
    }

    override fun contains(x: Double, y: Double): Boolean {
        return shape.contains(x, y)
    }

    private fun drawImpl(context: DrawContext, lineColor: Color, fillColor: Color?) {
        val oldColor = context.g.color
        if (fillColor != null) {
            context.g.color = fillColor
            context.g.fill(shape)
        }
        context.g.color = lineColor
        context.g.stroke = stroke
        context.g.draw(shape)

        shape.beginLineTerminator?.draw(context)
        shape.endLineTerminator?.draw(context)

        context.g.color = oldColor
    }

    /** ---- [Polyline] interface */

    override val pointsCount: Int
        get() = shape.pointsCount

    override var beginLineTerminator: LineTerminator?
        get() = shape.beginLineTerminator
        set(value) {
            invalidate()
            shape.beginLineTerminator = value
            invalidate()
            update()
        }

    override var endLineTerminator: LineTerminator?
        get() = shape.endLineTerminator
        set(value) {
            invalidate()
            shape.endLineTerminator = value
            invalidate()
            update()
        }

    override fun clear() {
        shape.clear()
    }

    override fun addPoint(x: Double, y: Double): Polyline {
        return addPointAt(pointsCount, x, y)
    }

    override fun addPointAt(index: Int, x: Double, y: Double): Polyline {
        invalidate()
        shape.addPointAt(index, x, y)
        invalidate()
        update()
        return this
    }

    override fun removePoint(index: Int): Polyline {
        invalidate()
        shape.removePoint(index)
        invalidate()
        update()
        return this
    }

    override fun getPointAt(index: Int): Point2D {
        return shape.getPointAt(index)
    }

    override fun setPointAt(index: Int, x: Double, y: Double): Polyline {
        invalidate()
        shape.setPointAt(index, x, y)
        invalidate()
        update()
        return this
    }

    override fun setPoints(points: List<Point2D>): Polyline {
        invalidate()
        shape.setPoints(points)
        invalidate()
        update()
        return this
    }

    override fun setLocation(x: Double, y: Double): Polyline {
        invalidate()
        shape.setLocation(x, y)
        invalidate()
        update()
        return this
    }

    override fun getLineWidth(): Double {
        return stroke.width.toDouble()
    }

    override fun findSegment(x: Double, y: Double, area: Int): Int? {
        return shape.findSegment(x, y, area)
    }

    override fun findPoint(x: Double, y: Double, area: Int): Int? {
        return shape.findPoint(x, y, area)
    }

    override fun getCenterOfSegment(index: Int): Point2D {
        return shape.getCenterOfSegment(index)
    }

    override fun compact(): Boolean {
	    invalidate()
        if (shape.compact()) {
	        invalidate()
	        update()
	        return true
        }
	    return false
    }

    override fun getLength(): Double {
        return shape.getLength()
    }

    override fun getSegmentLength(index: Int): Double {
        return shape.getSegmentLength(index)
    }

    override fun getPoints(startIndex: Int, endIndex: Int): List<Point2D> {
        return shape.getPoints(startIndex, endIndex)
    }

    override fun isSegmentOrthogonal(index: Int): Boolean {
        return shape.isSegmentOrthogonal(index)
    }

	override fun mirrorHorizontally(x: Double) {
		shape.mirrorHorizontally(x)
	}

	override fun mirrorVertically(y: Double) {
		shape.mirrorVertically(y)
	}
}