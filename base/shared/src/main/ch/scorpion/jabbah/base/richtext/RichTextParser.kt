package ch.scorpion.jabbah.base.richtext

import ch.scorpion.jabbah.base.parser.AbstractParser
import ch.scorpion.jabbah.base.dsl.Compound
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.DslTokenType.EOF

/**
 * Parses sentences by which users can textually describe text with rich properties,
 * such as overline, subscript, and superscript.
 *
 * ### Sample sentences
 * - Negation: ~A, ~(ABC)
 * - Subscript: A_1, A_(123)
 * - Superscript: A^1, A^(123)
 *
 * ### Syntax
 * ```
 * text : { fragment }
 * fragment : styledText [([subscript] [superscript] | [superscript] [subscript])]
 * styledText : simpleText
 *          | overline
 * simpleText : { CHAR }
 * overline : "!" CHAR | "!(" simpleText ")"
 * subscript : "_" CHAR | "_(" styledText ")"
 * superscript : "^" CHAR | "^(" styledText ")"
 * ```
 */
class RichTextParser(lexer: RichTextLexer) : AbstractParser(lexer) {

	constructor(text: String): this(RichTextLexer(text))

	override fun parse(): Node {
		return text()
	}

	private fun text(): Compound {
		val fragmentList = mutableListOf<Node>()
		while (currentToken!!.type != EOF) {
			fragmentList.add(fragment())
		}
		return Compound(lexer.location, fragmentList)
	}

	private fun fragment(): Node {
		val text = styledText()
		var subscript: StyledText? = null
		var superscript: StyledText? = null

		return Fragment(lexer.location, text, subscript, superscript)
	}

	private fun styledText(): StyledText {
		return StyledText(lexer.location, "Bla")
	}
}