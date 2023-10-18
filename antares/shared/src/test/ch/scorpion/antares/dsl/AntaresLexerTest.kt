package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.dsl.BaseTokenType
import ch.scorpion.jabbah.base.dsl.DslLexer
import ch.scorpion.jabbah.base.dsl.DslTokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresLexerTest {

	@Test
	fun shouldScanHexLiteral() {
		assertNumberLiteral(255, "0xFF")
		assertNumberLiteral(255, "0xff")
		assertNumberLiteral(7006, "0x1B5E")
	}

	@Test
	fun shouldScanUndefinedHexLiteral() {
		val lexer = AntaresLexer("a = 0x?4")
		assertId("a", lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		val token = lexer.nextToken()
		assertEquals(BaseTokenType.LITERAL, token.type)
		assertEquals(Word.undefined(BitWidth.BW_4), token.value)
	}

	@Test
	fun shouldScanBinaryLiteral() {
		assertNumberLiteral(0, "0b0")
		assertNumberLiteral(3, "0b11")
		assertNumberLiteral(9, "0b1001")
		assertNumberLiteral(255, "0b11111111")
	}

	private fun assertNumberLiteral(expected: Long, literal: String) {
		val lexer = AntaresLexer("a = $literal")
		assertId("a", lexer)
		assertToken(DslTokenType.ASSIGN, lexer)
		val token = lexer.nextToken()
		assertEquals(BaseTokenType.LITERAL, token.type)
		assertEquals(expected, token.value)
	}

	private fun assertId(name: String, lexer: DslLexer) {
		val token = lexer.nextToken()
		assertEquals(BaseTokenType.ID, token.type)
		assertEquals(name, token.value)
	}

	private fun assertToken(type: DslTokenType, lexer: DslLexer) {
		assertEquals(type, lexer.nextToken().type)
	}
}