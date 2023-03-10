package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.math.SIGMA
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * A [PointRange] is a [Sequence] of [Point2D]s between a begin and an end point.
 *
 * Also provides the streaming method [forEach] to iterate over a [PointRange]
 * without instantiating [Point2D] objects.
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

	/** Holds the value to be returned next as individual coordinates.*/
	private var valueX: Double? = begin.x
	private var valueY: Double? = begin.y

	/** Holds the results from [calculateNextXY]. */
	private var nextX: Double? = null
	private var nextY: Double? = null

	var remainder: Double = 0.0
		private set

	init {
		if (initialOffset != null && initialOffset > SIGMA) {
			calculateNextXY(initialOffset)
			valueX = nextX
			valueY = nextY
		}
	}

    /** ---- [Sequence] interface */

    override val size: Double get() = _size

    override fun hasNext(): Boolean = valueX != null && valueY != null

    override fun getNext(distance: Double): Point2D {
		val oldValueX = valueX
	    val oldValueY = valueY

	    getNextXY(distance)

	    return Point2D(oldValueX!!, oldValueY!!)
    }

    override fun getCurrent(): Point2D {
        if (valueX == null || valueY == null) {
            throw IllegalStateException("no current value")
        }
        return Point2D(valueX!!, valueY!!)
    }

	/** ---- [PointRange] sequencing API for avoiding [Point2D] instantiation */

	fun forEach(distance: Double, handler: (x: Double, y: Double) -> Unit) {
		while (hasNext()) {
			val oldValueX = valueX
			val oldValueY = valueY

			getNextXY(distance)
			handler(oldValueX!!, oldValueY!!)
		}
	}

	private fun getNextXY(distance: Double) {
		if (valueX == null || valueY == null) {
			throw NoSuchElementException("distance")
		}

		calculateNextXY(distance)

		valueX = nextX
		valueY = nextY
	}

	private fun calculateNextXY(distance: Double) {
		require(distance >= 0) { "distance must not be negative" }

		if (size <= SIGMA) {
			nextX = null
			nextY = null
			return
		}

		val dx = (end.x - begin.x) / size * distance
		val dy = (end.y - begin.y) / size * distance

		nextX = valueX!! + dx
		nextY = valueY!! + dy
		val d = begin.distance(nextX!!, nextY!!)

		if (d >= size) {
			remainder = d - size
			if (d == size || returnEndPoint) {
				nextX = end.x
				nextY = end.y
			} else {
				nextX = null
				nextY = null
				return
			}
		}

		if (nextX == valueX!! && nextY == valueY!!) {
			nextX = null
			nextY = null
		}
	}
}