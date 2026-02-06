package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.BaseTokenType
import ch.scorpion.jabbah.base.parser.AbstractLexer
import ch.scorpion.jabbah.base.parser.TokenType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestcaseLexerTest {

	@BeforeTest
	fun setup() {
		Translations.withAnyKey()
	}

	@Test
	fun shouldScanRun() {
		val lexer = TestcaseLexer("""
			run {
				0 1
			}
		""".trimIndent())

		assertToken(TestcaseTokenType.RUN, lexer)
		assertToken(TestcaseTokenType.LCURLEY, lexer)
		assertToken(BaseTokenType.EOL, lexer)
		assertLong(0L, lexer)
		assertLong(1L, lexer)
		assertToken(BaseTokenType.EOL, lexer)
		assertToken(TestcaseTokenType.RCURLEY, lexer)
	}

	@Test
	fun shouldScanIdentifiers() {
		val lexer = TestcaseLexer("I1 I2 O")
		assertId("I1", lexer)
		assertId("I2", lexer)
		assertId("O", lexer)
	}

	@Test
	fun shouldScanInOutAsInput() {
		val lexer = TestcaseLexer("I1 >IO O")
		assertId("I1", lexer)
		assertToken(TestcaseTokenType.GREATER, lexer)
		assertId("IO", lexer)
		assertId("O", lexer)
	}

	@Test
	fun shouldScanInOutAsOutput() {
		val lexer = TestcaseLexer("I1 <IO O")
		assertId("I1", lexer)
		assertToken(TestcaseTokenType.SMALLER, lexer)
		assertId("IO", lexer)
		assertId("O", lexer)
	}

	@Test
	fun shouldScanXandZasIdentifiers() {
		val lexer = TestcaseLexer("X X2 Z")
		assertId("X", lexer)
		assertId("X2", lexer)
		assertId("Z", lexer)
	}

	private fun assertToken(type: TokenType, lexer: AbstractLexer) {
		assertEquals(type, lexer.nextToken().type)
	}

	private fun assertLong(value: Long, lexer: AbstractLexer) {
		val token = lexer.nextToken()
		assertEquals(TestcaseTokenType.DECIMAL_LITERAL, token.type)
		assertEquals(DigitalSignalFactory.of(BitWidth.BW_1, value), token.value)
	}

	private fun assertId(name: String, lexer: AbstractLexer) {
		val token = lexer.nextToken()
		assertEquals(BaseTokenType.ID, token.type)
		assertEquals(name, token.value)
	}

}