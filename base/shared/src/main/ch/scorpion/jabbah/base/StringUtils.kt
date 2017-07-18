package ch.scorpion.jabbah.base

/**
 * [String] utilities.
 */
object StringUtils {

    fun isBlank(s: String?): Boolean {
        return s == null || s.isBlank()
    }

    fun isEmpty(s: String?): Boolean {
        return s == null || s.isEmpty()
    }

    fun isNotEmpty(s: String?): Boolean = !isEmpty(s)

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
}