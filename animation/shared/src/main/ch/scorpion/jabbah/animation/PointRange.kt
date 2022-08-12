package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.SIGMA
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * A [PointRange] is a [Sequence] of [Point2D]s between a begin and an end point.
 *
 * @param returnEndPoint set to `false` only if this [PointRange] is part of a sequence of consecutive
 * [PointRanges][PointRange], where returning the end point of a segment would be doubled by
 * the begin point of the following segment
 * @param initialOffset used if sequencing should not start at [begin], but with the given offset from [begin].
 * Used with sequences of consecutive [PointRanges][PointRange] in order to regain "speed" on the current segment
 * that has been lost by a remainder of the previous segment
 */
class PointRange(
	val begin: Point2D,
	val end: Point2D,
	private val returnEndPoint: Boolean = true,
	initialOffset: Double? = null
	) : Sequence<Point2D> {

    private val _size: Double = begin.distance(end)

	private var lastReached: Boolean = false

    /** Holds the value to be returned next.*/
    private var value: Point2D? = begin

	private var initialOffset: Double = initialOffset ?: 0.0

	var remainder: Double = 0.0
		private set

    /** ---- [Sequence] interface */

    override val size: Double
        get() = _size

    override fun hasNext(): Boolean = value != null

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
	    require(distance >= 0) { "distance must not be negative" }

	    if (lastReached) {
	    	value = null
		    return
	    }

	    if (size <= SIGMA) {
            value = null
            return
        }

	    val effDistance = distance + initialOffset
	    initialOffset = 0.0

        val dx = (end.x - begin.x) / size * effDistance
        val dy = (end.y - begin.y) / size * effDistance

        value = Point2D(value!!.x + dx, value!!.y + dy)
	    val d = value!!.distance(begin)
        if (d >= size) {
			remainder = d - size
        	lastReached = true
	        value = if (returnEndPoint) {
		        end
	        } else {
		        null
	        }
        }
    }
}