package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.exception.UnsupportedOperationException

/**
 * A wrapper for a math class specific to a target environment.
 */
open class MathClass {

	companion object {
		const val PI: Double = 3.14159265358979323846
		const val TWO_PI: Double = 2.0 * PI
		const val PI_2: Double = PI / 2.0
		const val PI_4: Double = PI / 4.0
		const val E: Double = 2.7182818284590452354
		const val SIGMA: Double = 0.0000001
	}

	open fun min(a: Double, b: Double): Double = throw UnsupportedOperationException()

	open fun min(a: Long, b: Long): Long = throw UnsupportedOperationException()

	open fun min(a: Int, b: Int): Int = throw UnsupportedOperationException()

	open fun max(a: Double, b: Double): Double = throw UnsupportedOperationException()

	open fun max(a: Long, b: Long): Long = throw UnsupportedOperationException()

	open fun max(a: Int, b: Int): Int = throw UnsupportedOperationException()

	open fun abs(a: Double): Double = throw UnsupportedOperationException()

	open fun abs(a: Int): Int = throw UnsupportedOperationException()

	/**
	 * Returns the signum function of the argument; zero if the argument
	 * is zero, 1.0 if the argument is greater than zero, -1.0 if the
	 * argument is less than zero.
	 */
	open fun signum(a: Double): Double = throw UnsupportedOperationException()

	open fun floor(a: Double): Double = throw UnsupportedOperationException()

	open fun ceil(a: Double): Double = throw UnsupportedOperationException()

	open fun sqrt(a: Double): Double = throw UnsupportedOperationException()

	open fun sin(a: Double): Double = throw UnsupportedOperationException()

	open fun cos(a: Double): Double = throw UnsupportedOperationException()

	open fun atan(a: Double): Double = throw UnsupportedOperationException()

	open fun round(a: Double): Long = throw UnsupportedOperationException()

	open fun random(): Double = throw UnsupportedOperationException()

	open fun toDegrees(radians: Double): Double = throw UnsupportedOperationException()

	open fun log10(a: Double): Double = throw UnsupportedOperationException()

	/** Returns the natural logarithm (base e) of `a`.*/
	open fun ln(a: Double): Double = throw UnsupportedOperationException()

	/** Returns the value of `a` raised to the power of `b`.*/
	open fun power(a: Double, b: Double): Double = throw UnsupportedOperationException()

	fun random(min: Double, max: Double) = random() * (max - min) + min

	fun randomInt(min: Int, max: Int): Int {
		val minCeil = ceil(min.toDouble()).toInt()
		val maxFloor = floor(max.toDouble()).toInt()
		return floor(random() * (maxFloor - minCeil + 1)).toInt() + minCeil
	}
}

var Math: MathClass = MathClass()