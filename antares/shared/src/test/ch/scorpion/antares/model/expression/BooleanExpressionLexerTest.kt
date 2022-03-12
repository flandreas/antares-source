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
}