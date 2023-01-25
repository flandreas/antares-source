package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.math.SIGMA
import kotlin.math.abs

/**
 * A [Sequence] of [Double] values bound by two boundary values (inclusive).
 *
 * Kotlin's [Number] class doesn't define arithmetic operations, hence we can't build
 * a generic NumberRange class.
 */
class DoubleRange(
	val begin: Double,
	val end: Double
) : CloneableSequence<Double> {

	private var value: Double? = begin

	private var lastReached: Boolean = false

	/** ---- [Sequence] interface*/

	override val size: Double = abs(begin - end)

	override fun hasNext(): Boolean = value != null

	override fun getNext(distance: Double): Double {
		if (value == null) {
			throw NoSuchElementException("distance")
		}
		val result = value
		calculateNext(distance)
		return result!!
	}

	override fun getCurrent(): Double = value ?: throw IllegalStateException("no current value")

	override fun clone(reversed: Boolean): CloneableSequence<Double> = if (reversed) {
		DoubleRange(end, begin)
	} else {
		DoubleRange(begin, end)
	}

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

		if (end >= begin) {
			value = value!! + distance
			if (value!! >= end) {
				lastReached = true
				value = end
			}
		} else {
			value = value!! - distance
			if (value!! <= end) {
				lastReached = true
				value = end
			}
		}
	}
}