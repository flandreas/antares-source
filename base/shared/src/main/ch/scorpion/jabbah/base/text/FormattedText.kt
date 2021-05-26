package ch.scorpion.jabbah.base.text

/**
 * Represents a text (non-HTML) with simple formatting information.
 * The text [String] can contain special Unicode character representing some format information.
 */
class FormattedText(
	val text: String,
	val allNegated: Boolean = false
) {

	companion object {

		private const val NEGATION_SIGN = '!'
		const val OVERLINE = '\u0305'

		fun replaceNegation(s: String): FormattedText {
			var negating = false
			var inBlock = false
			val result = StringBuilder()
			val resultAllNegated = StringBuilder()
			var allNegated = s.startsWith(NEGATION_SIGN)

			for (c in s) {
				when (c) {
					NEGATION_SIGN -> {
						negating = true
					}
					'(' -> {
						if (negating) {
							inBlock = true
						} else {
							result.append(c)
							resultAllNegated.append(c)
						}
					}
					')' -> {
						if (negating) {
							inBlock = false
							negating = false
						} else {
							result.append(c)
							resultAllNegated.append(c)
						}
					}
					else -> {
						result.append(c)
						resultAllNegated.append(c)
						if (negating) {
							result.append(OVERLINE)
						} else {
							allNegated = false
						}
						if (!inBlock) {
							negating = false
						}
					}
				}
			}
			return if (allNegated) {
				FormattedText(resultAllNegated.toString(), allNegated = true)
			} else {
				FormattedText(result.toString(), allNegated = false)
			}
		}
	}
}