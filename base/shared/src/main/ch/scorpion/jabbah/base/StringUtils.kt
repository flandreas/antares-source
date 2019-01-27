package ch.scorpion.jabbah.base

/**
 * [String] utilities.
 */
object StringUtils {

    private const val NEGATION_SIGN = '!'
    const val OVERLINE = '\u0305'

    fun isBlank(s: String?): Boolean {
        return s == null || s.isBlank()
    }

    fun isEmpty(s: String?): Boolean {
        return s == null || s.isEmpty()
    }

    fun isNotEmpty(s: String?): Boolean = !isEmpty(s)

	fun isNotBlank(s: String?): Boolean = !isBlank(s)

    fun orEmpty(s: String?): String {
        if (s == null) {
            return ""
        }
        return s
    }

    /** Counts the number of occurrences of a particular [Char] in a [String].*/
    fun countChar(s: String, c: Char): Int {
        return s.length - s.replace(c.toString(), "").length
    }

    fun replaceNegation(s: String): String {
        return s.replace("$NEGATION_SIGN(.)".toRegex(), "${'$'}1" + OVERLINE)
    }

	/** Returns a [String] that adds a period to the specified [String] if if doesn't already end with a period.*/
	fun endWithPeriod(s: String): String {
		return if (s.endsWith(".")) s else "$s."
	}
}