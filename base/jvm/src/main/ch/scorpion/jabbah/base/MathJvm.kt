package ch.scorpion.jabbah.base


/**
 * A wrapper class for the JVM Math class.
 */
class MathJvm : MathClass() {

	override fun min(a: Double, b: Double): Double = java.lang.Math.min(a, b)

	override fun min(a: Long, b: Long): Long = java.lang.Math.min(a, b)

	override fun min(a: Int, b: Int): Int = java.lang.Math.min(a, b)

	override fun max(a: Double, b: Double): Double = java.lang.Math.max(a, b)

	override fun max(a: Int, b: Int): Int = java.lang.Math.max(a, b)

	override fun max(a: Long, b: Long): Long = java.lang.Math.max(a, b)

	override fun abs(a: Double): Double = java.lang.Math.abs(a)

	override fun abs(a: Int): Int = java.lang.Math.abs(a)

	override fun signum(a: Double): Double = java.lang.Math.signum(a)

	override fun floor(a: Double): Double = java.lang.Math.floor(a)

	override fun ceil(a: Double): Double = java.lang.Math.ceil(a)

	override fun sqrt(a: Double): Double = java.lang.Math.sqrt(a)

	override fun sin(a: Double): Double = java.lang.Math.sin(a)

	override fun atan(a: Double): Double = java.lang.Math.atan(a)

	override fun cos(a: Double): Double = java.lang.Math.cos(a)

	override fun round(a: Double): Long = java.lang.Math.round(a)

	override fun random(): Double = java.lang.Math.random()

	override fun toDegrees(radians: Double): Double = java.lang.Math.toDegrees(radians)

	override fun ln(a: Double): Double = java.lang.Math.log(a)

	override fun log10(a: Double): Double = java.lang.Math.log10(a)

	override fun power(a: Double, b: Double): Double = java.lang.Math.pow(a, b)
}