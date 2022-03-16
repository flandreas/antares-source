package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.dsl.TokenType
import kotlin.test.Test

class BooleanExpressionLexerTest : AbstractLexerTest() {

	@Test
	fun shouldScanArithmeticExpression() {
		val lexer = BooleanExpressionLexer("X = A * B' + A' * B")

		assertId("X", lexer)
		assertToken(TokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(TokenType.MULTIPLY, lexer)
		assertId("B", lexer)
		assertToken(TokenType.SINGLE_QUOTE, lexer)
		assertToken(TokenType.PLUS, lexer)
		assertId("A", lexer)
		assertToken(TokenType.SINGLE_QUOTE, lexer)
		assertToken(TokenType.MULTIPLY, lexer)
		assertId("B", lexer)
	}

	@Test
	fun shouldScanMultiLineArithmeticExpression() {
		val lexer = BooleanExpressionLexer("""
			X = A + BIN
			Y = BIN
		""".trimIndent(), singleCharIdentifier = false)

		assertId("X", lexer)
		assertToken(TokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(TokenType.PLUS, lexer)
		assertId("BIN", lexer)

		assertId("Y", lexer)
		assertToken(TokenType.ASSIGN, lexer)
		assertId("BIN", lexer)
	}

	@Test
	fun shouldScanLogicExpression() {
		val lexer = BooleanExpressionLexer("X = A ∧ ¬B ∨ ¬A ∧ B")

		assertId("X", lexer)
		assertToken(TokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(TokenType.LOGIC_AND, lexer)
		assertToken(TokenType.LOGIC_NOT, lexer)
		assertId("B", lexer)
		assertToken(TokenType.LOGIC_OR, lexer)
		assertToken(TokenType.LOGIC_NOT, lexer)
		assertId("A", lexer)
		assertToken(TokenType.LOGIC_AND, lexer)
		assertId("B", lexer)
	}

	@Test
	fun shouldScanProgrammingExpression() {
		val lexer = BooleanExpressionLexer("X = A && !B || !A && B")

		assertId("X", lexer)
		assertToken(TokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(TokenType.PROGRAMMING_AND, lexer)
		assertToken(TokenType.PROGRAMMING_NOT, lexer)
		assertId("B", lexer)
		assertToken(TokenType.PROGRAMMING_OR, lexer)
		assertToken(TokenType.PROGRAMMING_NOT, lexer)
		assertId("A", lexer)
		assertToken(TokenType.PROGRAMMING_AND, lexer)
		assertId("B", lexer)
	}

	@Test
	fun shouldScanVerboseExpression() {
		val lexer = BooleanExpressionLexer("X = A AND NOT B OR NOT A AND B")

		assertId("X", lexer)
		assertToken(TokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(TokenType.AND, lexer)
		assertToken(TokenType.NOT, lexer)
		assertId("B", lexer)
		assertToken(TokenType.OR, lexer)
		assertToken(TokenType.NOT, lexer)
		assertId("A", lexer)
		assertToken(TokenType.AND, lexer)
		assertId("B", lexer)
	}

	@Test
	fun shouldScanSingleCharacterIdentifiers() {
		val lexer = BooleanExpressionLexer("X = AB' + A'B", singleCharIdentifier = true)

		assertId("X", lexer)
		assertToken(TokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertId("B", lexer)
		assertToken(TokenType.SINGLE_QUOTE, lexer)
		assertToken(TokenType.PLUS, lexer)
		assertId("A", lexer)
		assertToken(TokenType.SINGLE_QUOTE, lexer)
		assertId("B", lexer)
	}

	@Test
	fun shouldScanMultiLineSingleCharacterIdentifiers() {
		val lexer = BooleanExpressionLexer("""
			X = A + B
			Y = B
		""".trimIndent(), singleCharIdentifier = true)

		assertId("X", lexer)
		assertToken(TokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(TokenType.PLUS, lexer)
		assertId("B", lexer)

		assertId("Y", lexer)
		assertToken(TokenType.ASSIGN, lexer)
		assertId("B", lexer)
	}

	@Test
	fun shouldScanMultiLineSingleWithTrailingNegationCharacterIdentifiers() {
		val lexer = BooleanExpressionLexer("""
			X = A + B'
			Y' = B
		""".trimIndent(), singleCharIdentifier = true)

		assertId("X", lexer)
		assertToken(TokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(TokenType.PLUS, lexer)
		assertId("B", lexer)
		assertToken(TokenType.SINGLE_QUOTE, lexer)

		assertId("Y", lexer)
		assertToken(TokenType.SINGLE_QUOTE, lexer)
		assertToken(TokenType.ASSIGN, lexer)
		assertId("B", lexer)
	}
}