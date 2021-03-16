package ch.scorpion.jabbah.base

import kotlin.math.PI
import kotlin.math.roundToInt

const val TWO_PI: Double = 2.0 * PI
const val PI_2: Double = PI / 2.0
const val PI_4: Double = PI / 4.0
const val SIGMA: Double = 0.0000001
const val MILLION: Long = 1_000_000

private const val ROUND_PRECISION = 1000.0

/**
 * In absence of BigDecimal or String.format() on Kotlin multi-platform, this
 * method produces a [String] representation of a [Double] with 3 decimal places.
 * This can be used for serializing [Double]s to persistent representations.
 */
fun Double.formatRounded(): String =
	((this * ROUND_PRECISION).roundToInt() / ROUND_PRECISION).toString()
