package ch.scorpion.jabbah.base

/**
 * Implements the [Math] interface on the JavaScript platform.
 */
class MathJs : MathClass() {

    override fun min(a: Int, b: Int): Int = kotlin.js.Math.min(a, b)
    override fun min(a: Long, b: Long): Long = kotlin.js.Math.min(a, b)
    override fun min(a: Double, b: Double): Double = kotlin.js.Math.min(a, b)

    override fun max(a: Int, b: Int): Int = kotlin.js.Math.max(a, b)
    override fun max(a: Long, b: Long): Long = kotlin.js.Math.max(a, b)
    override fun max(a: Double, b: Double): Double = kotlin.js.Math.max(a, b)

    override fun abs(a: Int): Int = kotlin.js.Math.abs(a.toDouble()).toInt()
    override fun abs(a: Double): Double = kotlin.js.Math.abs(a)

    override fun signum(a: Double): Double {
        if (a == 0.0) {
            return 0.0
        } else if (a < 0.0) {
            return -1.0
        } else {
            return 1.0
        }
    }

    override fun floor(a: Double): Double = kotlin.js.Math.floor(a).toDouble()

    override fun ceil(a: Double): Double = kotlin.js.Math.ceil(a).toDouble()

    override fun sqrt(a: Double): Double = kotlin.js.Math.sqrt(a)

    override fun sin(a: Double): Double = kotlin.js.Math.sin(a)

    override fun atan(a: Double): Double = kotlin.js.Math.atan(a)

    override fun cos(a: Double): Double = kotlin.js.Math.cos(a)

    override fun round(a: Double): Long = kotlin.js.Math.round(a).toLong()

    override fun random(): Double = kotlin.js.Math.random()
}