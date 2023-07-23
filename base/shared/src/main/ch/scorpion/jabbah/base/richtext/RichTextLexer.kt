package ch.scorpion.jabbah.base.richtext

import ch.scorpion.jabbah.base.parser.AbstractLexer
import ch.scorpion.jabbah.base.parser.Token
import ch.scorpion.jabbah.base.parser.TokenType
import ch.scorpion.jabbah.base.richtext.RichTextTokenType.*

enum class RichTextTokenType(override val id: String): TokenType {
	TEXT("text"),
	LPAREN("("),
	RPAREN(")"),
	OVERLINE("!"),
	SUBSCRIPT("_"),
	SUPERSCRIPT("^"),
	EOF("EOF")
}

class RichTextLexer(text: String) : AbstractLexer(text) {

	companion object {

		private val LPAREN_TOKEN = Token<Unit>(LPAREN)
		private val RPAREN_TOKEN = Token<Unit>(RPAREN)
		private val OVERLINE_TOKEN = Token<Unit>(OVERLINE)
		private val SUBSCRIPT_TOKEN = Token<Unit>(SUBSCRIPT)
		private val SUPERSCRIPT_TOKEN = Token<Unit>(SUPERSCRIPT)
		private val EOF_TOKEN = Token<Unit>(EOF)

		private val TEXT_END_CHARS = listOf('!', '_', '^', ')')
		private val SINGLE_CHAR_TEXT_CHARS = listOf('!', '_', '^')
	}

	/** Determines whether only a single character is consumed when reading the next text in [nextToken].*/
	private var singleCharMode = false

	override fun nextToken(state: State): Token<Any> {
		recordLocation(state)

		if (state.currentChar == null) {
			return EOF_TOKEN
		}

		when (state.currentChar!!) {
			'(' -> return advanceByUpdatingSingleCharMode(state, LPAREN_TOKEN)
			')' -> return advanceByUpdatingSingleCharMode(state, RPAREN_TOKEN)
			'!' -> return advanceByUpdatingSingleCharMode(state, OVERLINE_TOKEN)
			'_' -> return advanceByUpdatingSingleCharMode(state, SUBSCRIPT_TOKEN)
			'^' -> return advanceByUpdatingSingleCharMode(state, SUPERSCRIPT_TOKEN)
		}

		return if (singleCharMode) {
			Token(TEXT, character())
		} else {
			Token(TEXT, text())
		}
	}

	private fun advanceByUpdatingSingleCharMode(state: State, token: Token<Any>): Token<Any> {
		this.singleCharMode = SINGLE_CHAR_TEXT_CHARS.contains(state.currentChar)
		return advanceWith(state, token)
	}

	private fun text(): String {
		val text = StringBuilder()
		while (state.currentChar != null && !TEXT_END_CHARS.contains(state.currentChar)) {
			text.append(state.currentChar)
			advance(state)
		}
		return text.toString()
	}

	private fun character(): String {
		return if (state.currentChar != null && !TEXT_END_CHARS.contains(state.currentChar)) {
			val s = state.currentChar!!.toString()
			advance(state)
			s
		} else {
			String()
		}
	}
}