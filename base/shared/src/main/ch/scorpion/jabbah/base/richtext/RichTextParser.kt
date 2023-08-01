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
 * richText : { styledFragment }
 * styledFragment : simpleFragment | boldFragment
 * simpleFragment : styledText [([subscript] [superscript] | [superscript] [subscript])]
 * boldFragment : "*(" simpleFragment ")"
 * styledText : { styledChunk }
 * styledChunk : text | overline | bold
 * overline : "!" ( singleChar | "(" styledText ")" )
 * bold : "*" ( singleChar | "(" styledText ")" )
 * text : { CHAR }
 * subscript : "_" ( singleChar | "(" styledText ")" )
 * superscript : "^" ( singleChar | "(" styledText ")" )
 * ```
 */
class RichTextParser(lexer: RichTextLexer) : AbstractParser(lexer) {

	companion object {
		fun bold(text: String): String = "*($text)"
	}

	constructor(text: String): this(RichTextLexer(text))

	private var style: TextStyle = TextStyle.NORMAL

	override fun parse(): RichText = richText()

	private fun richText(): RichText {
		val fragmentList = mutableListOf<Fragment>()
		while (currentToken!!.type != EOF) {
			fragmentList.add(styledFragment())
		}
		return RichText(lexer.location, fragmentList)
	}

	private fun styledFragment(): Fragment {
		return when (currentToken!!.type) {
			BOLD -> boldFragment()
			else -> fragment()
		}
	}

	private fun boldFragment(): Fragment {
		eat(BOLD)
		eat(LPAREN)
		val fragment = fragment(true)
		eat(RPAREN)
		return fragment
	}

	private fun fragment(bold: Boolean = false): Fragment {
		lexer.location.let { location ->
			style = if (bold) TextStyle.BOLD else TextStyle.NORMAL

			val text = FragmentText(location, styledText())
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

			return Fragment(location, text, bold, subscript, superscript)
		}
	}

	private fun styledText(): StyledText {
		lexer.location.let { location ->
			val chunks = mutableListOf<StyledChunk>()
			chunks.addAll(styledChunk().chunks)
			while (isStyledChunk()) {
				chunks.addAll(styledChunk().chunks)
			}
			return StyledText(location, chunks)
		}
	}

	private fun isStyledChunk(): Boolean {
		return when (currentToken!!.type) {
			TEXT -> true
			OVERLINE -> true
			BOLD -> true
			else -> false
		}
	}

	private fun styledChunk(): StyledText {
		lexer.location.let { location ->
			val token = currentToken!!
			return when (currentToken!!.type) {
				OVERLINE -> overline()
				BOLD -> bold()
				TEXT -> text()
				else -> throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", token.type.id))
			}
		}
	}

	private fun text(): StyledText {
		lexer.location.let { location ->
			val token = currentToken!!
			eat(TEXT)
			return StyledText(lexer.location, listOf(StyledChunk(location, token.value as String, style)))
		}
	}

	private fun overline(): StyledText {
		eat(OVERLINE)

		style = TextStyle.withOverline(style)
		val overline = if (currentToken!!.type == LPAREN) {
			eat(LPAREN)
			val styledText = styledText()
			eat(RPAREN)
			styledText
		} else {
			text()
		}
		style = TextStyle.withoutOverline(style)

		return overline
	}

	private fun bold(): StyledText {
		eat(BOLD)

		style = TextStyle.withBold(style)
		val bold = if (currentToken!!.type == LPAREN) {
			eat(LPAREN)
			val styledText = styledText()
			eat(RPAREN)
			styledText
		} else {
			text()
		}
		style = TextStyle.withoutBold(style)

		return bold
	}

	private fun subscript(): Subscript {
		eat(SUBSCRIPT)
		return when (currentToken!!.type) {
			LPAREN -> {
				eat(LPAREN)
				val subscript = Subscript(lexer.location, styledText())
				eat(RPAREN)
				subscript
			}
			OVERLINE -> Subscript(lexer.location, overline())
			BOLD -> Subscript(lexer.location, bold())
			else -> Subscript(lexer.location, singleChar())
		}
	}

	private fun superscript(): Superscript {
		eat(SUPERSCRIPT)
		return when (currentToken!!.type) {
			LPAREN -> {
				eat(LPAREN)
				val superscript = Superscript(lexer.location, styledText())
				eat(RPAREN)
				superscript
			}
			OVERLINE -> Superscript(lexer.location, overline())
			BOLD -> Superscript(lexer.location, bold())
			else -> Superscript(lexer.location, singleChar())
		}
	}

	private fun singleChar(): StyledText = text()
}