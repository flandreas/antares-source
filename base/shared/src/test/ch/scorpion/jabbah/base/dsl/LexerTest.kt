package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.TokenType.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LexerTest {

	@BeforeTest
	fun setup() {
		Translations.withAnyKey()
	}

	@Test
	fun shouldScanMultiplyTerm() {
		val lexer = Lexer("7*12")

		assertLong(7, lexer)
		assertToken(MULTIPLY, lexer)
		assertLong(12, lexer)
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanDivideTerm() {
		val lexer = Lexer("48/6")

		assertLong(48, lexer)
		assertToken(DIVIDE, lexer)
		assertLong(6, lexer)
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanPlusExpression() {
		val lexer = Lexer("12+8")

		assertLong(12, lexer)
		assertToken(PLUS, lexer)
		assertLong(8, lexer)
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanMinusExpression() {
		val lexer = Lexer("23-11")

		assertLong(23, lexer)
		assertToken(MINUS, lexer)
		assertLong(11, lexer)
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanTermWithParentheses() {
		val lexer = Lexer("3 * (15 - 7)")

		assertLong(3, lexer)
		assertToken(MULTIPLY, lexer)
		assertToken(LPAREN, lexer)
		assertLong(15, lexer)
		assertToken(MINUS, lexer)
		assertLong(7, lexer)
		assertToken(RPAREN, lexer)
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanAssignment() {
		val lexer = Lexer("a = 17")

		assertId("a", lexer)
		assertToken(ASSIGN, lexer)
		assertLong(17, lexer)
	}

	@Test
	fun shouldPeekNextToken() {
		val lexer = Lexer("a = 17")

		assertId("a", lexer)
		assertEquals(ASSIGN, lexer.peekNextToken().type)
		assertToken(ASSIGN, lexer)
		assertEquals(LITERAL, lexer.peekNextToken().type)
		assertLong(17, lexer)
	}

	@Test
	fun shouldSkipStartLineComment() {
		val lexer = Lexer("""
			a = 5
			// a = 12
			a
		""".trimIndent())

		assertId("a", lexer)
		assertToken(ASSIGN, lexer)
		assertLong(5, lexer)
		assertId("a", lexer)
	}

	@Test
	fun shouldSkipMidLineComment() {
		val lexer = Lexer("a = 5 // + 7")
		assertId("a", lexer)
		assertToken(ASSIGN, lexer)
		assertLong(5, lexer)
		assertToken(EOF, lexer)
	}

	@Test
	fun shouldScanBlock() {
		val lexer = Lexer("""
			{
				a = 12
			}
		""".trimIndent())

		assertToken(LCURLEY, lexer)
		assertId("a", lexer)
		assertToken(ASSIGN, lexer)
		assertLong(12, lexer)
		assertToken(RCURLEY, lexer)
	}

	@Test
	fun shouldScanVar() {
		val lexer = Lexer("var a")

		assertToken(VAR, lexer)
		assertId("a", lexer)
	}

	@Test
	fun shouldScanStore() {
		val lexer = Lexer("store a")

		assertToken(STORE, lexer)
		assertId("a", lexer)
	}

	@Test
	fun shouldScanEqual() {
		val lexer = Lexer("a == b")
		assertId("a", lexer)
		assertToken(EQUAL, lexer)
		assertId("b", lexer)
	}

	@Test
	fun shouldScanDiff() {
		val lexer = Lexer("a != b")
		assertId("a", lexer)
		assertToken(DIFF, lexer)
		assertId("b", lexer)
	}

	@Test
	fun shouldScanSmaller() {
		val lexer = Lexer("a < b")
		assertId("a", lexer)
		assertToken(SMALLER, lexer)
		assertId("b", lexer)
	}

	@Test
	fun shouldScanGreater() {
		val lexer = Lexer("a > b")
		assertId("a", lexer)
		assertToken(GREATER, lexer)
		assertId("b", lexer)
	}

	@Test
	fun shouldScanSmallerEqual() {
		val lexer = Lexer("a <= b")
		assertId("a", lexer)
		assertToken(SMALLER_EQUAL, lexer)
		assertId("b", lexer)
	}

	@Test
	fun shouldScanGreaterEqual() {
		val lexer = Lexer("a >= b")
		assertId("a", lexer)
		assertToken(GREATER_EQUAL, lexer)
		assertId("b", lexer)
	}

	@Test
	fun shouldScanIfThen() {
		val lexer = Lexer("if (5) {}")
		assertToken(IF, lexer)
		assertToken(LPAREN, lexer)
		assertLong(5, lexer)
		assertToken(RPAREN, lexer)
		assertToken(LCURLEY, lexer)
		assertToken(RCURLEY, lexer)
	}

	@Test
	fun shouldScanIfThenElse() {
		val lexer = Lexer("if (5) { 17 } else { 42 }")
		assertToken(IF, lexer)
		assertToken(LPAREN, lexer)
		assertLong(5, lexer)
		assertToken(RPAREN, lexer)
		assertToken(LCURLEY, lexer)
		assertLong(17, lexer)
		assertToken(RCURLEY, lexer)
		assertToken(ELSE, lexer)
		assertToken(LCURLEY, lexer)
		assertLong(42, lexer)
		assertToken(RCURLEY, lexer)
	}

	@Test
	fun shouldScanAnd() {
		val lexer = Lexer("5 and 7")
		assertLong(5, lexer)
		assertToken(AND, lexer)
		assertLong(7, lexer)
	}

	@Test
	fun shouldScanOr() {
		val lexer = Lexer("5 or 7")
		assertLong(5, lexer)
		assertToken(OR, lexer)
		assertLong(7, lexer)
	}

	@Test
	fun shouldScanNot() {
		val lexer = Lexer("not 2")
		assertToken(NOT, lexer)
		assertLong(2, lexer)
	}

	@Test
	fun shouldScanShiftLeft() {
		val lexer = Lexer("4 << 1")
		assertLong(4, lexer)
		assertToken(SHIFT_LEFT, lexer)
		assertLong(1, lexer)
	}

	@Test
	fun shouldScanShiftRight() {
		val lexer = Lexer("4 >> 1")
		assertLong(4, lexer)
		assertToken(SHIFT_RIGHT, lexer)
		assertLong(1, lexer)
	}

	@Test
	fun shouldScanMod() {
		val lexer = Lexer("5 % 2")
		assertLong(5, lexer)
		assertToken(MOD, lexer)
		assertLong(2, lexer)
	}

	@Test
	fun shouldScanWhen() {
		val lexer = Lexer("""
			when (a) {
				1 : 11
				2 : 22
				else : 33
			}
		""".trimIndent())

		assertToken(WHEN, lexer)
		assertToken(LPAREN, lexer)
		assertId("a", lexer)
		assertToken(RPAREN, lexer)
		assertToken(LCURLEY, lexer)
		assertLong(1L, lexer)
		assertToken(COLON, lexer)
		assertLong(11L, lexer)
		assertLong(2L, lexer)
		assertToken(COLON, lexer)
		assertLong(22L, lexer)
		assertToken(ELSE, lexer)
		assertToken(COLON, lexer)
		assertLong(33L, lexer)
		assertToken(RCURLEY, lexer)
	}

	@Test
	fun shouldScanFor() {
		val lexer = Lexer("""
			for (a in 1 to 10) {
				b
			}
		""".trimIndent())

		assertToken(FOR, lexer)
		assertToken(LPAREN, lexer)
		assertId("a", lexer)
		assertToken(IN, lexer)
		assertLong(1, lexer)
		assertToken(TO, lexer)
		assertLong(10, lexer)
		assertToken(RPAREN, lexer)
		assertToken(LCURLEY, lexer)
		assertId("b", lexer)
		assertToken(RCURLEY, lexer)
	}

	@Test
	fun shouldScanQuotedId() {
		assertId("!Q", Lexer("'!Q'"))
		assertId("ID with blanks", Lexer("'ID with blanks'"))
	}

	@Test
	fun shouldContinueAfterQuotedId() {
		val lexer = Lexer("a 'A B' c")

		assertId("a", lexer)
		assertId("A B", lexer)
		assertId("c", lexer)
	}

	@Test
	fun shouldExpectClosingSingleQuote() {
		assertFailsWith(SyntaxError::class) {
			Lexer("'A").nextToken()
		}
	}

	@Test
	fun shouldNotAcceptEmptyQuotedId() {
		assertFailsWith(SyntaxError::class) {
			Lexer("''").nextToken()
		}
	}

	@Test
	fun shouldScanAssocArrayValue() {
		val lexer = Lexer("a[0]")
		assertId("a", lexer)
		assertToken(LEFT_BRACKET, lexer)
		assertLong(0, lexer)
		assertToken(RIGHT_BRACKET, lexer)
	}

	@Test
	fun shouldScanString() {
		val lexer = Lexer("\"text\"")
		assertToken(DOUBLE_QUOTE, lexer)
		assertId("text", lexer)
		assertToken(DOUBLE_QUOTE, lexer)
	}

	private fun assertEof(token: Token<Any>) {
		assertEquals(EOF, token.type)
	}

	private fun assertLong(value: Long, lexer: Lexer) {
		val token = lexer.nextToken()
		assertEquals(LITERAL, token.type)
		assertEquals(value, token.value)
	}

	private fun assertId(name: String, lexer: Lexer) {
		val token = lexer.nextToken()
		assertEquals(ID, token.type)
		assertEquals(name, token.value)
	}

	private fun assertToken(type: TokenType, lexer: Lexer) {
		assertEquals(type, lexer.nextToken().type)
	}
}