package io.antarescircuit.jabbah.base

import io.antarescircuit.jabbah.base.math.near
import io.antarescircuit.jabbah.base.math.toDoubleString
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

object Thousands {

	/** The total number of digits in the result.*/
	private const val DIGIT_COUNT = 3

	private const val MILLI_CHAR = 'm'
	private const val MICRO_CHAR = 'µ'
	private const val NANO_CHAR = 'n'

	private const val KILO_CHAR = 'K'
	private const val KILO_MIN = 1_000L

	private const val MEGA_CHAR = 'M'
	private const val MEGA_MIN = 1_000_000L

	private const val GIGA_CHAR = 'G'
	private const val GIGA_MIN = 1_000_000_000L

	private const val TERA_CHAR = 'T'
	private const val TERA_MIN = 1_000_000_000_000L

	private const val BIG_MIN = 1_000_000_000_000_000L

	/**
	 * Converts the specified value to a [String] with a predefined number of relevant digits,
	 * using the commonly knows abbreviation letter for multiples of thousand, such as K (Kilo) for
	 * thousands or M (Mega) for millions.
	 *
	 * @param value the value to be converted
	 * @return the converted value, such as "12.3K" for "12345, or "BIG" if `value` is larger not smaller
	 * than 1_000_000_000_000_000.
	 */
	fun convert(value: Long, separator: String = ""): String = convert(value.toDouble(), separator)

	fun convert(value: Double, separator: String = ""): String {
		return if (value >= 1) {
			val log = log10(value).toInt()
			when {
				value < KILO_MIN -> "${value.toLong()}$separator"
				value < MEGA_MIN -> "${round((value / KILO_MIN.toDouble()), DIGIT_COUNT - (log - 3 + 1))}$separator$KILO_CHAR"
				value < GIGA_MIN -> "${round((value / MEGA_MIN.toDouble()), DIGIT_COUNT - (log - 6 + 1))}$separator$MEGA_CHAR"
				value < TERA_MIN -> "${round((value / GIGA_MIN.toDouble()), DIGIT_COUNT - (log - 9 + 1))}$separator$GIGA_CHAR"
				value < BIG_MIN -> "${round((value / TERA_MIN.toDouble()), DIGIT_COUNT - (log - 12 + 1))}$separator$TERA_CHAR"
				else -> "BIG"
			}
		} else {
			when  {
				floor(value * 100) == value * 100.0 -> "$value$separator"
				floor(value * 1_000).near(value * 1_000.0) -> "${round(value * KILO_MIN, 3)}$separator$MILLI_CHAR"
				floor(value * 1_000_000).near(value * 1_000_000.0) -> "${round(value * MEGA_MIN, 3)}$separator$MICRO_CHAR"
				floor(value * 1_000_000_000).near(value * 1_000_000_000.0) -> "${round(value * GIGA_MIN, 3)}$separator$NANO_CHAR"
				else -> "SMALL"
			}
		}
	}

	/** Rounds `value` to at most `digits` digits after the comma, removing unnecessary trailing zeros and commas.*/
	fun round(value: Double, digits: Int): String {
		val tenPower = 10.0.pow(digits.toDouble())
		val s = ((value * tenPower).toInt() / tenPower).toDoubleString()
		return s.trimEnd('0').trimEnd('.')
	}
}