package ch.scorpion.jabbah.base.richtext

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.Node
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
 * - Bold: *A, *(ABC), *(A_1)
 * - Italic: /A, /(ABC), /(A_1)
 *
 * Subscript and superscripts can also contain negation.
 *
 * ### Syntax
 * ```
 * richText : { styledFragment }
 * styledFragment : fragment | boldText | italicText
 * fragment : styledChunk [([subscript] [superscript] | [superscript] [subscript])]
 * boldText : "*(" richText ")"
 * italicText : "/(" richText ")"
 * subscript : "_" ( CHAR | "(" styledText ")" )
 * superscript : "^" ( CHAR | "(" styledText ")" )
 * styledText : { styledChunk }
 * styledChunk : text | overline | bold
 * text : { CHAR }
 * overline : "!" ( singleChar | "(" styledText ")" )
 * bold : "*" ( singleChar | "(" styledText ")" )
 * singleChar : CHAR
 * ```
 */
class RichTextParser(lexer: RichTextLexer) : AbstractParser(lexer) {

	companion object {

		fun bold(text: String): String =
			when (text.length) {
				0 -> ""
				1 -> "${BOLD.id}$text"
				else -> "${BOLD.id}${LPAREN.id}$text${RPAREN.id}"
			}

		fun italic(text: String): String =
			when (text.length) {
				0 -> ""
				1 -> "${ITALIC.id}$text"
				else -> "${ITALIC.id}${LPAREN.id}$text${RPAREN.id}"
			}

		fun negated(text: String): String =
			when (text.length) {
				0 -> ""
				1 -> "${OVERLINE.id}$text"
				else -> "${OVERLINE.id}${LPAREN.id}$text${RPAREN.id}"
			}
	}

	constructor(text: String): this(RichTextLexer(text))

	private var style: TextStyle = TextStyle.NORMAL

	private fun <T: Node> eatParen(expr: () -> T): T {
		eat(LPAREN)
		val result = expr()
		eat(RPAREN)
		return result
	}

	override fun parse(): RichText = richText()

	private fun richText(): RichText {
		lexer.location.let { location ->
			val fragments = mutableListOf<Fragment>()
			while (currentToken!!.type != EOF && currentToken!!.type != RPAREN) {
				fragments.addAll(styledFragment())
			}
			return RichText(location, fragments)
		}
	}

	private fun styledFragment(): List<Fragment> {
		return when (currentToken!!.type) {
			BOLD -> boldText().children
			ITALIC -> italicText().children
			else -> listOf(fragment())
		}
	}

	private fun boldText(): RichText {
		eat(BOLD)
		style = TextStyle.withBold(style)
		val richText = eatParen { richText() }
		style = TextStyle.withoutBold(style)
		return richText
	}

	private fun italicText(): RichText {
		eat(ITALIC)
		style = TextStyle.withItalic(style)
		val richText = eatParen { richText() }
		style = TextStyle.withoutItalic(style)
		return richText
	}

	private fun fragment(): Fragment {
		lexer.location.let { location ->
			val text = FragmentText(location, styledChunk())
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

			return Fragment(location, text, subscript, superscript)
		}
	}

	private fun styledText(): StyledText {
		lexer.location.let { location ->
			val chunks = mutableListOf<StyledChunk>()
			while (isStyledChunk()) {
				chunks.addAll(styledChunk().chunks)
			}
			return StyledText(location, chunks)
		}
	}

	private fun subscript(): Subscript {
		eat(SUBSCRIPT)
		return when (currentToken!!.type) {
			LPAREN -> {
				eatParen { Subscript(lexer.location, styledText()) }
			}
			OVERLINE -> Subscript(lexer.location, overline())
			BOLD -> Subscript(lexer.location, bold())
			ITALIC -> Subscript(lexer.location, italic())
			else -> Subscript(lexer.location, singleChar())
		}
	}

	private fun superscript(): Superscript {
		eat(SUPERSCRIPT)
		return when (currentToken!!.type) {
			LPAREN -> {
				eatParen { Superscript(lexer.location, styledText()) }
			}
			OVERLINE -> Superscript(lexer.location, overline())
			BOLD -> Superscript(lexer.location, bold())
			ITALIC -> Superscript(lexer.location, italic())
			else -> Superscript(lexer.location, singleChar())
		}
	}

	private fun isStyledChunk(): Boolean {
		return when (currentToken!!.type) {
			TEXT -> true
			OVERLINE -> true
			BOLD -> true
			ITALIC -> true
			else -> false
		}
	}

	private fun styledChunk(): StyledText {
		lexer.location.let { location ->
			val token = currentToken!!
			return when (currentToken!!.type) {
				OVERLINE -> overline()
				BOLD -> bold()
				ITALIC -> italic()
				TEXT -> text()
				else -> throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", token.type.id))
			}
		}
	}

	private fun bold(): StyledText {
		eat(BOLD)

		style = TextStyle.withBold(style)
		val bold = if (currentToken!!.type == LPAREN) {
			eatParen { styledText() }
		} else {
			singleChar()
		}
		style = TextStyle.withoutBold(style)

		return bold
	}

	private fun italic(): StyledText {
		eat(ITALIC)

		style = TextStyle.withItalic(style)
		val italic = if (currentToken!!.type == LPAREN) {
			eatParen { styledText() }
		} else {
			singleChar()
		}
		style = TextStyle.withoutItalic(style)

		return italic
	}

	private fun overline(): StyledText {
		eat(OVERLINE)

		style = TextStyle.pushOverline(style)
		val overline = if (currentToken!!.type == LPAREN) {
			eatParen { styledText() }
		} else {
			singleChar()
		}
		style = TextStyle.popOverline(style)

		return overline
	}

	private fun text(): StyledText {
		lexer.location.let { location ->
			if (currentToken!!.type != TEXT) {
				throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", currentToken!!.type.id))
			}
			var text = currentToken!!.value as String
			eat(TEXT)
			return StyledText(lexer.location, listOf(StyledChunk(location, text, style)))
		}
	}

	/** [RichTextLexer] will be in single-char mode and only return 1 character.*/
	private fun singleChar(): StyledText = text()
}