package io.antarescircuit.jabbah.base.geom

import kotlin.math.max
import kotlin.math.min

interface MutableRectangularShape : RectangularShape {

    /** Moves this [MutableRectangularShape] by the specified translation vector.*/
    fun moveBy(v: Point2D): MutableRectangularShape {
        setFrame(x + v.x, y + v.y, width, height)
        return this
    }

    /** Sets the location and size of the outer bounds of this [MutableRectangularShape].*/
    fun setFrame(x: Double, y: Double, width: Double, height: Double)

    fun setFrame(x: Int, y: Int, width: Int, height: Int) {
        setFrame(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
    }

    /** Sets the location and size of this [MutableRectangularShape] to those of the specified [MutableRectangularShape].*/
    fun setFrame(rect: RectangularShape) {
        setFrame(rect.x, rect.y, rect.width, rect.height)
    }

    /** Sets the location and dimension of this [MutableRectangularShape] to the corresponding argument values.*/
    fun setFrame(location: Point2D, dimension: Dimension2D) {
        setFrame(location.x, location.y, dimension.width, dimension.height)
    }

    /**
     * Adds a point, specified by its coordinates, to this [MutableRectangularShape].
     * The resulting [MutableRectangularShape] is the smallest rectangle that contains both the original [RectangularShape]
     * and the specified point.
     * @return this [MutableRectangularShape] to support method chaining
     */
    fun add(x: Double, y: Double): MutableRectangularShape {
        if (isInitial) {
            setFrame(x, y, 0.0, 0.0)
        } else {
            val x1 = min(minX, x)
            val x2 = max(maxX, x)
            val y1 = min(minY, y)
            val y2 = max(maxY, y)
            setFrame(x1, y1, x2 - x1, y2 - y1)
        }
        return this
    }

    fun add(x: Int, y: Int): MutableRectangularShape {
        return add(x.toDouble(), y.toDouble())
    }

    fun add(p: Point2D): MutableRectangularShape {
        return add(p.x, p.y)
    }

    /**
     * Adds the specified [RectangularShape] to this [MutableRectangularShape]. The resulting [MutableRectangularShape]
     * is the union of the two [RectangularShape]s.
     * @return this [MutableRectangularShape] to support method chaining
     */
    fun add(rect: RectangularShape): MutableRectangularShape {
        if (isInitial) {
            setFrame(rect)
        } else {
            val x1 = min(minX, rect.minX)
            val x2 = max(maxX, rect.maxX)
            val y1 = min(minY, rect.minY)
            val y2 = max(maxY, rect.maxY)
            setFrame(x1, y1, x2 - x1, y2 - y1)
        }
        return this
    }

    /**
     * Expands this [MutableRectangularShape] by adding the corresponding delta to each side of this [MutableRectangularShape].
     */
    fun expandBy(deltaX: Double, deltaY: Double): MutableRectangularShape {
        setFrame(x - deltaX, y - deltaY, width + 2 * deltaX, height + 2 * deltaY)
        return this
    }

    /** Expands this [MutableRectangularShape] by adding the delta to each side of this [MutableRectangularShape].*/
    fun expandBy(delta: Double): MutableRectangularShape = expandBy(delta, delta)

    fun expandBy(topY: Double, leftX: Double, bottomY: Double, rightX: Double): MutableRectangularShape {
        setFrame(x - leftX, y - topY, width + leftX + rightX, height + topY + bottomY)
        return this
    }

    fun expandLeftBy(deltaX: Double): MutableRectangularShape {
        setFrame(x - deltaX, y, width + deltaX, height)
        return this
    }
}