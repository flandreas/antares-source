package ch.scorpion.antares.hdl.vhdl

import ch.scorpion.antares.hdl.HDLRenaming
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.richtext.RichTextTokenType

class VHDLRenaming : HDLRenaming {

	companion object {
		private val KEYWORDS = setOf("abs", "access", "after", "alias", "all", "and", "architecture", "array", "assert",
			"attribute", "begin", "block", "body", "buffer", "bus", "case", "component", "configuration",
			"constant", "disconnect", "downto", "else", "elsif", "end", "entity", "exit", "file",
			"for", "function", "generate", "generic", "group", "guarded", "if", "impure", "in",
			"inertial", "inout", "is", "label", "library", "linkage", "literal", "loop",
			"map", "mod", "nand", "new", "next", "nor", "not", "null", "of",
			"on", "open", "or", "others", "out", "package", "port", "postponed", "procedure",
			"process", "pure", "range", "record", "register", "reject", "rem", "report", "return",
			"rol", "ror", "select", "severity", "signal", "shared", "sla", "sll", "sra",
			"srl", "subtype", "then", "to", "transport", "type", "unaffected", "units", "until",
			"use", "variable", "wait", "when", "while", "with", "xnor", "xor")
	}

	override fun checkName(name: String): String {
		var effName = name
		if (StringUtils.isBlank(effName)) {
			throw HDLException("Name must not be blank")
		}
		if (isKeyword(effName)) {
			return "p_$effName"
		}
		if (effName[0].isDigit()) {
			effName = "n$effName"
		}
		return cleanName(effName)
	}

	private fun isKeyword(name: String): Boolean = KEYWORDS.contains(name.lowercase())

	private fun cleanName(name: String): String {
		val sb = StringBuilder()
		for (c in name) {
			if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9') {
				sb.append(c)
			} else {
				when (c) {
					RichTextTokenType.OVERLINE.id[0], '~', '\u00AC' -> sb.append("not")
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