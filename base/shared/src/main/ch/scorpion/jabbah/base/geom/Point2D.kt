package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.Math

/**
 * The [Point2D] class defines a point representing a location in (x,y) coordinate space.
 * Designed to be immutable.
 */
data class Point2D(val x: Double = 0.0, val y: Double = 0.0) {

	companion object {
		val ZERO = Point2D()
	}

    constructor(p: Point2D): this(p.x, p.y)
    constructor(x: Int, y: Int): this(x.toDouble(), y.toDouble())

    override fun toString(): String = "Point2D($x,$y)"

    /** Returns the square of the distance from this [Point2D] to a specified location.*/
    fun distanceSq(x: Double, y: Double): Double{
        val px = x - this.x
        val py = y - this.y
        return px * px + py * py
    }

    /** Returns the square of the distance from this [Point2D] to a specified other [Point2D].*/
    fun distanceSq(p: Point2D): Double = distanceSq(p.x, p.y)

    /** Returns the distance from this [Point2D] to a specified location.*/
    fun distance(x: Double, y: Double): Double = Math.sqrt(distanceSq(x, y))

    /** Returns the distance from this [Point2D] to a specified other [Point2D].*/
    fun distance(p: Point2D): Double = distance(p.x, p.y)

    /** Creates a new [Point2D] by mirroring this [Point2D] at a vertical axis defined by its x-coordinate.*/
    fun mirrorHorizontally(x: Double) = Point2D(x + (x - this.x), this.y)

    /** Creates a new [Point2D] by mirroring this [Point2D] at a horizontal axis defined by its y-coordinate.*/
    fun mirrorVertically(y: Double) = Point2D(this.x, y + (y - this.y))

    /** Determines whether this [Point2D] is near the specified point in respect of the sensitive [area].*/
    fun isNear(x: Double, y: Double, area: Int): Boolean {
        return this.x >= x - area && this.x <= x + area && this.y >= y - area && this.y <= y + area
    }

    /** Returns a new [Point2D] by adding this [Point2D] to the specified offset.*/
    fun add(dx: Double, dy: Double): Point2D {
        return Point2D(x + dx, y + dy)
    }

    /** Returns a new [Point2D] by adding this [Point2D] to the specified offset.*/
    fun add(p: Point2D): Point2D {
        return add(p.x, p.y)
    }

    /** Creates a new [Point2D] that is the result of subtracting the specified [Point2D] from this [Point2D].*/
    fun subtract(p: Point2D): Point2D {
        return Point2D(x - p.x, y - p.y)
    }

    /** Creates a new [Point2] by multiplying the coordinates of this [Point2D] with the specified factor.*/
    fun multiply(factor: Double): Point2D {
        return Point2D(x * factor, y * factor)
    }

    /**
     * Returns a [Rectangle2D] of the specified half size whose center is this [Point2D].
     * @param halfSize the half of the rectangle's size.
     */
    fun toRect(halfSize: Double): Rectangle2D {
        return Rectangle2D(x - halfSize, y - halfSize, 2 * halfSize, 2 * halfSize)
    }
}
