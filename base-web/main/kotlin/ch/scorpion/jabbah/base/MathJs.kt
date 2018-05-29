package ch.scorpion.jabbah.base

/**
 * Implements the [Math] interface on the JavaScript platform.
 */
class MathJs : MathClass() {

	override fun min(a: Int, b: Int): Int = kotlin.math.min(a, b)
	override fun min(a: Long, b: Long): Long = kotlin.math.min(a, b)
	override fun min(a: Double, b: Double): Double = kotlin.math.min(a, b)

	override fun max(a: Int, b: Int): Int = kotlin.math.max(a, b)
	override fun max(a: Long, b: Long): Long = kotlin.math.max(a, b)
	override fun max(a: Double, b: Double): Double = kotlin.math.max(a, b)

	override fun abs(a: Int): Int = kotlin.math.abs(a.toDouble()).toInt()
	override fun abs(a: Double): Double = kotlin.math.abs(a)

	override fun signum(a: Double): Double {
		if (a == 0.0) {
			return 0.0
		} else if (a < 0.0) {
			return -1.0
		} else {
			return 1.0
		}
	}

	override fun floor(a: Double): Double = kotlin.math.floor(a).toDouble()

	override fun ceil(a: Double): Double = kotlin.math.ceil(a).toDouble()

	override fun sqrt(a: Double): Double = kotlin.math.sqrt(a)

	override fun sin(a: Double): Double = kotlin.math.sin(a)

	override fun atan(a: Double): Double = kotlin.math.atan(a)

	override fun cos(a: Double): Double = kotlin.math.cos(a)

	override fun round(a: Double): Long = kotlin.math.round(a).toLong()

	override fun random(): Double = kotlin.js.Math.random();

	override fun toDegrees(radians: Double): Double = radians * 180.0 / kotlin.math.PI
}