package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.hdl.vhdl.HDLException
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.richtext.RichTextTokenType

/**
 * Legalizes arbitrary circuit labels as HDL identifiers consisting of ASCII letters, digits and
 * single underscores. Names starting with a digit get an "n" prefix, and names colliding with one of
 * the language's reserved [keywords] get a "p_" prefix. Keywords are matched case-insensitively
 * unless the language [isCaseSensitive].
 */
abstract class AbstractHDLRenaming(
	private val keywords: Set<String>,
	override val isCaseSensitive: Boolean,
) : HDLRenaming {

	override fun checkName(name: String): String {
		if (StringUtils.isBlank(name)) {
			throw HDLException(Translations.getString("antares.vhdl.emptyName.error.text"))
		}
		val effName = cleanName(if (name[0].isDigit()) "n$name" else name)
		return if (isKeyword(effName)) "p_$effName" else effName
	}

	private fun isKeyword(name: String): Boolean =
		keywords.contains(if (isCaseSensitive) name else name.lowercase())

	private fun cleanName(name: String): String {
		val sb = StringBuilder()
		for (c in name) {
			if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9') {
				sb.append(c)
			} else {
				when (c) {
					RichTextTokenType.OVERLINE.id[0], '~', '¬' -> sb.append("not")
					'=' -> sb.append("_eq_")
					'<' -> sb.append("_lt_")
					'>' -> sb.append("_gt_")
					else -> {
						if (sb.isNotEmpty() && sb.last() != '_') {
							sb.append('_')
						}
					}
				}
			}
		}

		while (sb.isNotEmpty() && sb.last() == '_') {
			sb.deleteCharAt(sb.length - 1)
		}

		return sb.toString()
	}
}
