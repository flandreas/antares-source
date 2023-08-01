package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.base.richtext.RichTextLexer
import ch.scorpion.jabbah.graph.model.Graph

/**
 * Abbreviates the name of a [Graph] such that it is short enough to be
 * placed within a narrow symbol.
 */
object GraphNameAbbreviator {

	private const val LENGTH = 3

	fun abbreviate(name: String): String {
		val effName = name.filterNot { it.isWhitespace() || RichTextLexer.CONTROL_CHARS.contains(it) }

		if (effName.length <= LENGTH) {
			return effName
		}

		if (name.contains(' ')) {
			return name
				.split(' ')
				.map { it.filterNot { c -> c.isWhitespace() || RichTextLexer.CONTROL_CHARS.contains(c)} }
				.joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
				.take(LENGTH)
		}

		return effName.take(LENGTH).uppercase()
	}
}