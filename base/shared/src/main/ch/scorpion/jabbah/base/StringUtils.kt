package ch.scorpion.jabbah.base

/**
 * [String] utilities.
 */
object StringUtils {

    private const val NEGATION_SIGN = '!'
	private const val LIST_SEPARATOR = ','
	private const val LIST_ESCAPE = '\\'
    const val OVERLINE = '\u0305'

	private const val SPE = '\ufffe'  // unused unicode char in Specials block
	private const val SPF = '\uffff'  // dito

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

	/**
	 * Converts a [List] to a single, comma separated [String], and escapes the separating
	 * comma as needed.
	 */
	fun fromList(list: List<String>): String {
		return list.joinToString(
			separator = "$LIST_SEPARATOR"
		) {
			it
				.replace(LIST_ESCAPE.toString(), "$LIST_ESCAPE$LIST_ESCAPE")
				.replace(LIST_SEPARATOR.toString(), "$LIST_ESCAPE$LIST_SEPARATOR")
		}
	}

	/**
	 * Reverse operation of [fromList].
	 */
	fun toList(listString: String): List<String> {
		if (listString.isEmpty()) {
			return listOf()
		}

		var s = listString
			.replace("$LIST_ESCAPE$LIST_ESCAPE", "$SPE")
			.replace("$LIST_ESCAPE$LIST_SEPARATOR", "$SPF")

		s = if (s.last() == LIST_ESCAPE) // i.e. 'esc' not escaping anything
			s.dropLast(1).replace("$LIST_ESCAPE", "") + LIST_ESCAPE
		else
			s.replace("$LIST_ESCAPE", "")
		return s
			.split(LIST_SEPARATOR)
			.map { it
				.replace(SPE, LIST_ESCAPE)
				.replace(SPF, LIST_SEPARATOR)
			}
	}
}