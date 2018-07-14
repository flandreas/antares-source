package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.Math

/**
 * A [RectangularShape] is a [Shape] whose geometry is defined by a rectangular frame.
 */
interface RectangularShape : Shape {

    /** ---- [Shape] */

    override fun contains(x: Double, y: Double): Boolean =
        x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height

    override fun contains(x: Double, y: Double, width: Double, height: Double): Boolean {
        if (isEmpty || width <= 0 || height <= 0) {
            return false
        }
        return x >= this.x
            && x + width <= maxX
            && y >= this.y
            && y + height <= maxY
    }

    /** ---- [RectangularShape] */

    val x: Double
    val y: Double
    val width: Double
    val height: Double

    /** Determines whether this [RectangularShape]'s geometry property are all zero.*/
    val isInitial: Boolean get() = isEmpty && x == 0.0 && y == 0.0

    /** Determines whether this [RectangularShape] encloses no area.*/
    val isEmpty: Boolean get() = width <= 0 && height <= 0

    /** Contains the x coordinate of this [RectangularShape]'s center.  */
    val centerX: Double get() = x + width / 2.0

    /** Contains the y coordinate of this [RectangularShape]'s center.*/
    val centerY: Double get() = y + height / 2.0

    /** Contains the minimum x coordinate of this [RectangularShape].*/
    val minX: Double get() = x

    /** Contains the minimum y coordinate of this [RectangularShape].*/
    val minY: Double get() = y

    /** Contains the maximum x coordinate of this [RectangularShape].*/
    val maxX: Double get() = x + width

    /** Contains the maximum y coordinate of this [RectangularShape].*/
    val maxY: Double get() = y + height

	/** Contains the top-left edge of this [RectangularShape].*/
	val topLeft: Point2D get() = Point2D(minX, minY)

	/** Contains the bottom-right edge of this [RectangularShape].*/
	val bottomRight: Point2D get() = Point2D(maxX, maxY)

    /** Moves this [RectangularShape] by the specified translation vector.*/
    fun moveBy(v: Point2D): RectangularShape {
        setFrame(x + v.x, y + v.y, width, height)
        return this
    }

    /** Sets the location and size of the outer bounds of this [RectangularShape].*/
    fun setFrame(x: Double, y: Double, width: Double, height: Double)

    /** Sets the location and size of this [RectangularShape] to those of the specified [RectangularShape].*/
    fun setFrame(rect: RectangularShape) {
        setFrame(rect.x, rect.y, rect.width, rect.height)
    }

    /** Sets the location and dimension of this [RectangularShape] to the corresponding argument values.*/
    fun setFrame(location: Point2D, dimension: Dimension2D) {
        setFrame(location.x, location.y, dimension.width, dimension.height)
    }

    /**
     * Adds a point, specified by its coordinates, to this [RectangularShape]. The resulting [RectangularShape] is the smallest
     * rectangle that contains both the original [RectangularShape] and the specified point.
     * @return this [RectangularShape] to support method chaining
     */
    fun add(x: Double, y: Double): RectangularShape {
        if (isInitial) {
            setFrame(x, y, 0.0, 0.0)
        } else {
            val x1 = Math.min(minX, x)
            val x2 = Math.max(maxX, x)
            val y1 = Math.min(minY, y)
            val y2 = Math.max(maxY, y)
            setFrame(x1, y1, x2 - x1, y2 - y1)
        }
        return this
    }

    fun add(x: Int, y: Int): RectangularShape {
        return add(x.toDouble(), y.toDouble())
    }

    fun add(p: Point2D): RectangularShape {
        return add(p.x, p.y)
    }

    /**
     * Adds the specified [RectangularShape] to this [RectangularShape]. The resulting [RectangularShape] is the union of
     * the two [RectangularShape]s.
     * @return this [RectangularShape] to support method chaining
     */
    fun add(rect: RectangularShape): RectangularShape {
        if (isInitial) {
            setFrame(rect)
        } else {
            val x1 = Math.min(minX, rect.minX)
            val x2 = Math.max(maxX, rect.maxX)
            val y1 = Math.min(minY, rect.minY)
            val y2 = Math.max(maxY, rect.maxY)
            setFrame(x1, y1, x2 - x1, y2 - y1)
        }
        return this
    }

    /**
     * Expands this [RectangularShape] by adding the corresponding delta to each side of this [RectangularShape].
     */
    fun expandBy(deltaX: Double, deltaY: Double): RectangularShape {
        setFrame(x - deltaX, y - deltaY, width + 2 * deltaX, height + 2 * deltaY)
        return this
    }


    /** Expands this [RectangularShape] by adding the delta to each side of this [RectangularShape].*/
    fun expandBy(delta: Double): RectangularShape = expandBy(delta, delta)

    fun contains(rect: RectangularShape): Boolean {
        return contains(rect.x, rect.y, rect.width, rect.height)
    }
}