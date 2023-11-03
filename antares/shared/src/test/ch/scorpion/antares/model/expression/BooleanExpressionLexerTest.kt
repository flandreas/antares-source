package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.dsl.BaseTokenType
import ch.scorpion.jabbah.base.dsl.DslTokenType
import kotlin.test.Test

class BooleanExpressionLexerTest : AbstractLexerTest() {

	@Test
	fun shouldScanArithmeticExpression() {
		val lexer = BooleanExpressionLexer("X = A * B' + A' * B")

		assertId("X", lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(DslTokenType.MULTIPLY, lexer)
		assertId("B", lexer)
		assertToken(BaseTokenType.SINGLE_QUOTE, lexer)
		assertToken(DslTokenType.PLUS, lexer)
		assertId("A", lexer)
		assertToken(BaseTokenType.SINGLE_QUOTE, lexer)
		assertToken(DslTokenType.MULTIPLY, lexer)
		assertId("B", lexer)
	}

	@Test
	fun shouldScanMultiLineArithmeticExpression() {
		val lexer = BooleanExpressionLexer("""
			X = A + BIN
			Y = BIN
		""".trimIndent(), singleCharIdentifier = false)

		assertId("X", lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(DslTokenType.PLUS, lexer)
		assertId("BIN", lexer)

		assertId("Y", lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		assertId("BIN", lexer)
	}

	@Test
	fun shouldScanLogicExpression() {
		val lexer = BooleanExpressionLexer("X = A ∧ ¬B ∨ ¬A ∧ B")

		assertId("X", lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(DslTokenType.LOGIC_AND, lexer)
		assertToken(DslTokenType.LOGIC_NOT, lexer)
		assertId("B", lexer)
		assertToken(DslTokenType.LOGIC_OR, lexer)
		assertToken(DslTokenType.LOGIC_NOT, lexer)
		assertId("A", lexer)
		assertToken(DslTokenType.LOGIC_AND, lexer)
		assertId("B", lexer)
	}

	@Test
	fun shouldScanProgrammingExpression() {
		val lexer = BooleanExpressionLexer("X = A && !B || !A && B")

		assertId("X", lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(DslTokenType.PROGRAMMING_AND, lexer)
		assertToken(DslTokenType.PROGRAMMING_NOT, lexer)
		assertId("B", lexer)
		assertToken(DslTokenType.PROGRAMMING_OR, lexer)
		assertToken(DslTokenType.PROGRAMMING_NOT, lexer)
		assertId("A", lexer)
		assertToken(DslTokenType.PROGRAMMING_AND, lexer)
		assertId("B", lexer)
	}

	@Test
	fun shouldScanVerboseExpression() {
		val lexer = BooleanExpressionLexer("X = A AND NOT B OR NOT A AND B")

		assertId("X", lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(DslTokenType.AND, lexer)
		assertToken(DslTokenType.NOT, lexer)
		assertId("B", lexer)
		assertToken(DslTokenType.OR, lexer)
		assertToken(DslTokenType.NOT, lexer)
		assertId("A", lexer)
		assertToken(DslTokenType.AND, lexer)
		assertId("B", lexer)
	}

	@Test
	fun shouldScanSingleCharacterIdentifiers() {
		val lexer = BooleanExpressionLexer("X = AB' + A'B", singleCharIdentifier = true)

		assertId("X", lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertId("B", lexer)
		assertToken(BaseTokenType.SINGLE_QUOTE, lexer)
		assertToken(DslTokenType.PLUS, lexer)
		assertId("A", lexer)
		assertToken(BaseTokenType.SINGLE_QUOTE, lexer)
		assertId("B", lexer)
	}

	@Test
	fun shouldScanMultiLineSingleCharacterIdentifiers() {
		val lexer = BooleanExpressionLexer("""
			X = A + B
			Y = B
		""".trimIndent(), singleCharIdentifier = true)

		assertId("X", lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(DslTokenType.PLUS, lexer)
		assertId("B", lexer)

		assertId("Y", lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		assertId("B", lexer)
	}

	@Test
	fun shouldScanMultiLineSingleWithTrailingNegationCharacterIdentifiers() {
		val lexer = BooleanExpressionLexer("""
			X = A + B'
			Y' = B
		""".trimIndent(), singleCharIdentifier = true)

		assertId("X", lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		assertId("A", lexer)
		assertToken(DslTokenType.PLUS, lexer)
		assertId("B", lexer)
		assertToken(BaseTokenType.SINGLE_QUOTE, lexer)

		assertId("Y", lexer)
		assertToken(BaseTokenType.SINGLE_QUOTE, lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		assertId("B", lexer)
	}
}