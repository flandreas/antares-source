package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.dsl.TokenType
import kotlin.test.Test

class BooleanExpressionLexerTest : AbstractLexerTest() {

	@Test
	fun shouldScanExpression() {
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
}