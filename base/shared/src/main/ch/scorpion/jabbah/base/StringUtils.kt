package ch.scorpion.jabbah.base

import kotlin.js.JsName

/**
 * [String] utilities.
 */
object StringUtils {

    private const val NEGATION_SIGN = '!'
    const val OVERLINE = '\u0305'

    fun isBlank(s: String?): Boolean {
        return s == null || s.isBlank()
    }

	@JsName("isEmpty")
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

	fun orElse(s: String?, alternative: String): String {
		return if (!isBlank(s)) {
			s!!
		} else {
			alternative
		}
	}

    /** Counts the number of occurrences of a particular [Char] in a [String].*/
    fun countChar(s: String, c: Char): Int {
        return s.length - s.replace(c.toString(), "").length
    }

    fun replaceNegation(s: String): String {
	    var negating = false
	    var inBlock = false
	    val result = StringBuilder()

	    for (c in s) {
		    when (c) {
			    NEGATION_SIGN -> negating = true
			    '(' -> inBlock = true
			    ')' -> inBlock = false
			    else -> {
				    result.append(c)
				    if (negating) {
					    result.append(OVERLINE)
				    }
				    if (!inBlock) {
					    negating = false
				    }
			    }
		    }
	    }

	    return result.toString()
    }

	/** Returns a [String] that adds a period to the specified [String] if if doesn't already end with a period.*/
	fun endWithPeriod(s: String): String {
		return if (s.endsWith(".")) s else "$s."
	}

	/**
	 * Formats a [Long] using underscores to separate groups of 3 digits.
	 * Example: 12345678L is formatted as "12_345_678"
	 * */
	fun formatLong(l: Long): String {
		val s = l.toString()
		val result = StringBuilder()
		var i = 0
		for (c in s.reversed()) {
			result.append(c)
			i++
			if (i.rem(3) == 0 && i < s.length) {
				result.append('_')
			}
		}
		return result.toString().reversed()
	}
}