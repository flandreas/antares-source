package ch.scorpion.jabbah.base

object Thousands {

	/** The total number of digits in the result.*/
	private const val DIGIT_COUNT = 3

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
	 * thousand or M (Mega) for million.
	 *
	 * @param value the value to be converted
	 * @return the converted value, such as "12.3K" for "12345, or "BIG" if `value` is larger not smaller
	 * than 1_000_000_000_000_000.
	 */
	fun convert(value: Long): String {
		val log = Math.log10(value.toDouble()).toInt()

		return when {
			value < KILO_MIN -> value.toString()
			value < MEGA_MIN -> "${round((value / KILO_MIN.toDouble()), DIGIT_COUNT - (log - 3 + 1))}$KILO_CHAR"
			value < GIGA_MIN -> "${round(( value / MEGA_MIN.toDouble()), DIGIT_COUNT - (log - 6 + 1))}$MEGA_CHAR"
			value < TERA_MIN -> "${round((value / GIGA_MIN.toDouble()), DIGIT_COUNT - (log - 9 + 1))}$GIGA_CHAR"
			value < BIG_MIN -> "${round((value / TERA_MIN.toDouble()), DIGIT_COUNT - (log - 12 + 1))}$TERA_CHAR"
			else -> "BIG"
		}
	}

	/** Rounds `value` to at most `digits` digits after the comma, removing unnecessary trailing zeros and commas.*/
	fun round(value: Double, digits: Int): String {
		val tenPower = Math.power(10.0, digits.toDouble())
		val s = ((value * tenPower).toInt() / tenPower).toString()
		return s.trimEnd('0').trimEnd('.')
	}
}