package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.exception.UnsupportedOperationException

/**
 * A wrapper for a math class specific to a target environment.
 */
open class MathClass {

    companion object {
        val PI: Double = 3.14159265358979323846
        val E: Double = 2.7182818284590452354
    }

    open fun min(a: Double, b: Double): Double = throw UnsupportedOperationException()

    open fun min(a: Long, b: Long): Long = throw UnsupportedOperationException()

    open fun min(a: Int, b: Int): Int = throw UnsupportedOperationException()

    open fun max(a: Double, b: Double): Double = throw UnsupportedOperationException()

    open fun max(a: Long, b: Long): Long = throw UnsupportedOperationException()

    open fun max(a: Int, b: Int): Int = throw UnsupportedOperationException()

    open fun abs(a: Double): Double = throw UnsupportedOperationException()

    open fun abs(a: Int): Int = throw UnsupportedOperationException()

    open fun signum(a: Double): Double = throw UnsupportedOperationException()

    open fun floor(a: Double): Double = throw UnsupportedOperationException()

    open fun ceil(a: Double): Double = throw UnsupportedOperationException()

    open fun sqrt(a: Double): Double = throw UnsupportedOperationException()

    open fun sin(a: Double): Double = throw UnsupportedOperationException()

    open fun cos(a: Double): Double = throw UnsupportedOperationException()

    open fun atan(a: Double): Double = throw UnsupportedOperationException()

    open fun round(a: Double): Long = throw UnsupportedOperationException()

    open fun random(): Double = throw UnsupportedOperationException()

    fun random(min: Double, max: Double) = random() * (max - min) + min

    fun randomInt(min: Int, max: Int): Int {
        val min = ceil(min.toDouble()).toInt()
        val max = floor(max.toDouble()).toInt()
        return floor(random() * (max - min + 1)).toInt() + min
    }
}

var Math: MathClass = MathClass()