package ch.scorpion.antares.dsl

import ch.scorpion.antares.dsl.TokenType.*

/** Thrown by [Lexer.nextToken] if a syntax error is detected.*/
class SyntaxError(msg: String) : Throwable(msg)

/**
 * Lexical analyser, also known as scanner or tokenizer.
 *
 * This class is responsible for breaking a sentence apart into [Token]s, one [Token] at a time.
 * Inspects the [Char] at the current position, advances the current position, and
 * returns the [Token] that corresponds with the consumed [Char].
 *
 * @property text the text to be scanned
 */
class Lexer(private val text: String) {

	companion object {

		// Singleton instances of value-less [Token]s
		private val PLUS_TOKEN = Token<Unit>(PLUS)
		private val MINUS_TOKEN = Token<Unit>(MINUS)
		private val MULTIPLY_TOKEN = Token<Unit>(MULTIPLY)
		private val DIVIDE_TOKEN = Token<Unit>(DIVIDE)
		private val LPAREN_TOKEN = Token<Unit>(LPAREN)
		private val RPAREN_TOKEN = Token<Unit>(RPAREN)
		private val EOF_TOKEN = Token<Unit>(EOF)

		private fun plus() = PLUS_TOKEN
		private fun minus() = MINUS_TOKEN
		private fun multiply() = MULTIPLY_TOKEN
		private fun divide() = DIVIDE_TOKEN
		private fun lparen() = LPAREN_TOKEN
		private fun rparen() = RPAREN_TOKEN
		private fun eof() = EOF_TOKEN

		// Factory methods for [Token]s with values
		private fun integer(value: Int) = Token(INTEGER, value)
	}

	/** An index into [text].*/
	private var pos = 0

	/** Contains the [Char] in [text] at position [pos], or `null` if the end of [text] has been reached.*/
	private var currentChar: Char? = if (text.isEmpty()) null else text.first()

	/**
	 * Scans more text and returns the next [Token].
	 * @throws [SyntaxError] if a syntax error was detected
	 */
	fun nextToken(): Token<Any> {
		while (currentChar != null) {

			if (currentChar!!.isWhitespace()) {
				skipWhitespace()
				continue
			}

			if (isComment()) {
				advance()
				skipComment()
				continue
			}

			if (currentChar!!.isDigit()) {
				return number()
			}

			when (currentChar!!) {
				'/' -> {
					advance()
					return divide()
				}
				'+' -> {
					advance()
					return plus()
				}
				'-' -> {
					advance()
					return minus()
				}
				'*' -> {
					advance()
					return multiply()
				}
				'(' -> {
					advance()
					return lparen()
				}
				')' -> {
					advance()
					return rparen()
				}
			}
		}
		return eof()
	}

	/** Determines whether the current character is the begin of a comment.*/
	private fun isComment(): Boolean {
		return false
	}

	private fun skipComment() {
		// empty
	}

	/** Returns an [Int] consumed from the input. Subclasses might override this method to return other number types, such as [Double]s.*/
	private fun number(): Token<Any> {
		return Companion.integer(integer())
	}

	/** Returns the next [Char] (if any) without incrementing [pos].*/
	private fun peek(): Char? {
		val peekPos = pos + 1
		if (peekPos > text.length - 1) {
			return null
		}
		return text[peekPos]
	}

	/** Advances [pos] one position and updates [currentChar].*/
	private fun advance() {
		pos += 1
		currentChar = if (pos > text.length - 1) null else text[pos]
	}

	/** Advances until non-whitespace [currentChar] is non-whitespace.*/
	private fun skipWhitespace() {
		while (currentChar != null && currentChar!!.isWhitespace()) {
			advance()
		}
	}

	/** Returns a multi-digit [Int] consumed from the input text.*/
	private fun integer(): Int {
		val result = StringBuilder()
		while (currentChar != null && currentChar!!.isDigit()) {
			result.append(currentChar!!)
			advance()
		}
		return result.toString().toInt()
	}
}