package ch.scorpion.antares.dsl

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresInterpreterTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldInterpretHexLiteral() {
		val result = AntaresInterpreter("a = 0xFF").interpret()
		assertEquals(255L, result)
	}

	@Test
	fun shouldInterpretUndefinedHexLiteral() {
		val result = AntaresInterpreter("a = 0x?2").interpret()
		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_2), result)
	}

	@Test
	fun shouldGetBitOfLongVariable() {
		val result = AntaresInterpreter("""
			// Defined hex literals get converted to Long
			a = 0xF
			a@0
		""".trimIndent()).interpret()
		assertEquals(1L, result)
	}

	@Test
	fun shouldGetBitOfLongVariableWithTerm() {
		val result = AntaresInterpreter("""
			a = 0xF
			a@(1 + 1)
		""".trimIndent()).interpret()
		assertEquals(1L, result)
	}

	@Test
	fun shouldSetBitInLongVariable() {
		val result = AntaresInterpreter("""
			a = 0xF
			a@0 = 0
		""".trimIndent()).interpret()
		assertEquals(14L, result)
	}
}