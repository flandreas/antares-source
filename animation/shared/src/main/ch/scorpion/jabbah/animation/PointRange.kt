package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.SIGMA
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * A [PointRange] is a [Sequence] of [Point2D]s between a begin and an end point.
 */
class PointRange(val begin: Point2D, val end: Point2D) : Sequence<Point2D> {

    private val _size: Double = begin.distance(end)

	private var lastReached: Boolean = false

    /** Holds the value to be returned next.*/
    private var value: Point2D? = begin

    /** ---- [Sequence] interface */

    override val size: Double
        get() = _size

    override fun hasNext(): Boolean {
        return value != null
    }

    override fun getNext(distance: Double): Point2D {
        if (value == null) {
            throw NoSuchElementException("distance")
        }
        val result = value
        calculateNext(distance)
        return result!!
    }

    override fun getCurrent(): Point2D {
        if (value == null) {
            throw IllegalStateException("no current value")
        }
        return value!!
    }

    /** ---- [PointRange] */

    private fun calculateNext(distance: Double) {
        checkArgument(distance >= 0, "distance must not be negative")

	    if (lastReached) {
	    	value = null
		    return
	    }

	    if (size <= SIGMA) {
            value = null
            return
        }

        val dx = (end.x - begin.x) / size * distance
        val dy = (end.y - begin.y) / size * distance

        value = Point2D(value!!.x + dx, value!!.y + dy)
        if (value!!.distance(begin) >= size) {
        	lastReached = true
            value = end
        }
    }
}