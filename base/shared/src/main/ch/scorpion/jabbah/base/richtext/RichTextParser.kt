package ch.scorpion.jabbah.base.richtext

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.parser.AbstractParser
import ch.scorpion.jabbah.base.richtext.RichTextTokenType.*

/**
 * Parses sentences with which users can textually describe text with rich properties,
 * such as overline, subscript, and superscript.
 *
 * ### Sample sentences
 * - Negation: !A, !(ABC)
 * - Subscript: A_1, A_(123)
 * - Superscript: A^1, A^(123)
 *
 * Subscript and superscripts can also contain negation.
 *
 * ### Syntax
 * ```
 * richText : { fragment }
 * fragment : styledText [([subscript] [superscript] | [superscript] [subscript])]
 * styledText : styledChunk { styledChunk }
 * styledChunk : simpleText | overline
 * simpleText : { CHAR }
 * overline : "!" ( singleChar | "(" simpleText ")" )
 * subscript : "_" ( singleChar | overline | "(" styledText ")" )
 * superscript : "^" ( singleChar | overline | "(" styledText ")" )
 * singleChar : CHAR
 * ```
 */
class RichTextParser(lexer: RichTextLexer) : AbstractParser(lexer) {

	constructor(text: String): this(RichTextLexer(text))

	override fun parse(): RichText = richText()

	private fun richText(): RichText {
		val fragmentList = mutableListOf<Fragment>()
		while (currentToken!!.type != EOF) {
			fragmentList.add(fragment())
		}
		return RichText(lexer.location, fragmentList)
	}

	private fun fragment(): Fragment {
		val text = FragmentText(lexer.location, styledText())
		var subscript: Subscript? = null
		var superscript: Superscript? = null

		if (currentToken!!.type == SUBSCRIPT) {
			subscript = subscript()
		}
		if (currentToken!!.type == SUPERSCRIPT) {
			superscript = superscript()
			if (currentToken!!.type == SUBSCRIPT) {
				subscript = subscript()
			}
		}

		return Fragment(lexer.location, text, subscript, superscript)
	}

	private fun styledText(): StyledText {
		val chunks = mutableListOf<StyledChunk>()
		chunks.add(styledChunk())
		while (isStyledChunk()) {
			chunks.add(styledChunk())
		}
		return StyledText(lexer.location, chunks)
	}

	private fun isStyledChunk(): Boolean {
		return when (currentToken!!.type) {
			TEXT -> true
			OVERLINE -> true
			else -> false
		}
	}

	private fun styledChunk(): StyledChunk {
		val token = currentToken!!
		return when (currentToken!!.type) {
			OVERLINE -> overline()
			TEXT -> simpleText(TextStyle.NORMAL)
			else -> throw SyntaxError(lexer.location, Translations.getString("base.dsl.unexpectedToken.msg", token.type.id))
		}
	}

	private fun simpleText(style: TextStyle): StyledChunk {
		val token = currentToken!!
		eat(TEXT)
		return StyledChunk(lexer.location, token.value as String, style)
	}

	private fun overline(): StyledChunk {
		eat(OVERLINE)
		return if (currentToken!!.type == LPAREN) {
			eat(LPAREN)
			val styledText = styledChunk()
			eat(RPAREN)
			StyledChunk(lexer.location, styledText.text, TextStyle.OVERLINE)
		} else {
			simpleText(TextStyle.OVERLINE)
		}
	}

	private fun subscript(): Subscript {
		eat(SUBSCRIPT)
		return when (currentToken!!.type) {
			LPAREN -> {
				eat(LPAREN)
				val styledText = styledText()
				eat(RPAREN)
				Subscript(lexer.location, styledText)
			}
			OVERLINE -> Subscript(lexer.location, StyledText(lexer.location, listOf(overline())))
			else -> Subscript(lexer.location, singleChar())
		}
	}

	private fun superscript(): Superscript {
		eat(SUPERSCRIPT)
		return when (currentToken!!.type) {
			LPAREN -> {
				eat(LPAREN)
				val styledText = styledText()
				eat(RPAREN)
				Superscript(lexer.location, styledText)
			}
			OVERLINE -> Superscript(lexer.location, StyledText(lexer.location, listOf(overline())))
			else -> Superscript(lexer.location, singleChar())
		}
	}

	private fun singleChar(): StyledText =
		StyledText(lexer.location, listOf(simpleText(TextStyle.NORMAL)))
}