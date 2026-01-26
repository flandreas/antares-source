package ch.scorpion.antares.dsl

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.gate.CurrentUndefinedGateInputBehavior
import ch.scorpion.antares.model.gate.UndefinedGateInputBehavior
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresInterpreterTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun setup() {
		// This is used in expression evaluation
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0
	}

	@Test
	fun shouldInterpretHexLiteral() {
		val result = AntaresInterpreter("var a = 0xFF").interpret()
		assertEquals(255L, result)
	}

	@Test
	fun shouldInterpretUndefinedHexLiteral() {
		val result = AntaresInterpreter("var a = 0x?2").interpret()
		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_2), result)
	}

	@Test
	fun shouldGetBitOfLongVariable() {
		val result = AntaresInterpreter("""
			// Defined hex literals get converted to Long
			var a = 0xF
			a@0
		""".trimIndent()).interpret()
		assertEquals(1L, result)
	}

	@Test
	fun shouldGetBitOfLongVariableWithTerm() {
		val result = AntaresInterpreter("""
			var a = 0xF
			a@(1 + 1)
		""".trimIndent()).interpret()
		assertEquals(1L, result)
	}

	@Test
	fun shouldSetBitInLongVariable() {
		val result = AntaresInterpreter("""
			var a = 0xF
			a@0 = 0
		""".trimIndent()).interpret()
		assertEquals(14L, result)
	}

	@Test
	fun shouldInterpretStringLiteral() {
		val result = AntaresInterpreter("var a = \"test\"").interpret()
		assertEquals("test", result)
	}

	@Test
	fun shouldEvaluateEquals() {
		shouldBeTrue("5 == 5")
		shouldBeTrue("5.0 == 5")
		shouldBeTrue("5 == 5.0")
		shouldBeTrue("5.0 == 5.0")
		shouldBeTrue("0xF == 15")
		shouldBeTrue("15 == 0xF")
		shouldBeTrue("0x?8 == 0")
		shouldBeTrue("0x?8 == 0x?8")

		shouldBeFalse("5 == 6")
		shouldBeFalse("5.0 == 6")
		shouldBeFalse("6 == 5.0")
		shouldBeFalse("6.0 == 5.0")
		shouldBeFalse("0xF == 14")
		shouldBeFalse("14 == 0xF")
		shouldBeFalse("0x?8 == 1")
	}

	@Test
	fun shouldEvaluateSmaller() {
		shouldBeTrue("5 < 6")
		shouldBeTrue("5.0 < 6")
		shouldBeTrue("4 < 5.0")
		shouldBeTrue("4.0 < 5.0")
		shouldBeTrue("0xE < 15")
		shouldBeTrue("14 < 0xF")
		shouldBeTrue("0x?8 < 1")

		shouldBeFalse("6 < 5")
		shouldBeFalse("6.0 < 5")
		shouldBeFalse("6 < 5.0")
		shouldBeFalse("6.0 < 5.0")
		shouldBeFalse("0xF < 14")
		shouldBeFalse("15 < 0xF")
		shouldBeFalse("0x?8 < 0")
		shouldBeFalse("0x?8 < 0x?8")
	}

	@Test
	fun shouldEvaluateGreater() {
		shouldBeTrue("6 > 5")
		shouldBeTrue("6.0 > 5")
		shouldBeTrue("6 > 5.0")
		shouldBeTrue("6.0 > 5.0")
		shouldBeTrue("0xF > 14")
		shouldBeTrue("15 > 0xE")

		shouldBeFalse("5 > 6")
		shouldBeFalse("5.0 > 6")
		shouldBeFalse("4 > 5.0")
		shouldBeFalse("4.0 > 5.0")
		shouldBeFalse("0xE > 15")
		shouldBeFalse("14 > 0xF")
		shouldBeFalse("0x?8 > 1")
		shouldBeFalse("0x?8 > 0x?8")
	}

	private fun shouldBeTrue(exp: String) {
		assertEquals(1L, AntaresInterpreter(exp).interpret())
	}

	private fun shouldBeFalse(exp: String) {
		assertEquals(0L, AntaresInterpreter(exp).interpret())
	}

	@Test
	fun shouldCastLength() {
		val result = AntaresInterpreter("""
			var l = 16
			var a = 0x?8
			a${'$'}l
		""".trimIndent()).interpret()
		assertEquals(BitWidth.BW_16, (result as DigitalSignal).bitWidth)
	}

	@Test
	fun shouldNegate() {
		val result = AntaresInterpreter("not 0").interpret()
		assertEquals(1L, result)
	}
}