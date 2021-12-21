package ch.scorpion.antares.dsl

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.gate.CurrentUndefinedGateInputBehavior
import ch.scorpion.antares.model.gate.UndefinedGateInputBehavior
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_1
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_2
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_4
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_8
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.model.signal.Word.Companion.of
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.Symbol
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Unit tests for [AntaresInterpreter] using [DigitalSignal] values.*/
class AntaresInterpreterSignalTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun presetGlobalVariableWithSemanticAnalysis() {
		val program = """
			// a is preset by environment to 5
			var b = a + 1
		""".trimIndent()

		val analyser = SemanticAnalyser(null)
		analyser.scope.define(Symbol("a"))

		val parser = AntaresParser(AntaresLexer(program), analyser)

		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("a", 5L)

		val result = interpreter.interpret()

		assertEquals(6L, result)
	}

	@Test
	fun presetGlobalVariableWithoutSemanticAnalysis() {
		val program = """
			// a is preset by environment to 5
			var b = a + 1
		""".trimIndent()

		val parser = AntaresParser(AntaresLexer(program), null)

		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("a", 5L)

		val result = interpreter.interpret()

		assertEquals(6L, result)
	}

	@Test
	fun shouldInvertSignal() {
		val program = """
			var O = not I
		""".trimIndent()

		val parser = AntaresParser(AntaresLexer(program), null)

		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("I", of(BW_4, 2UL))

		val result = interpreter.interpret()

		assertEquals(of(BW_4, 13UL), result)
	}

	@Test
	fun shouldAddSignals() {
		assertEquals(of(BW_4, 3UL), operation("A + B", of(BW_4, 2UL), of(BW_2, 1UL)))
		assertEquals(of(BW_4, 15UL), operation("A + B", of(BW_4, 14UL), of(BW_2, 1UL)))
	}

	@Test
	fun shouldAddLongToSignal() {
		assertEquals(of(BW_4, 3UL), operation("A + B", of(BW_4, 2UL), 1L))
		assertEquals(of(BW_4, 15UL), operation("A + B", of(BW_4, 14UL), 1L))
	}

	@Test
	fun shouldSubtractSignals() {
		assertEquals(of(BW_4, 2UL), operation("A - B", of(BW_4, 3UL), of(BW_2, 1UL)))
		assertEquals(of(BW_4, 13UL), operation("A - B", of(BW_4, 14UL), of(BW_2, 1UL)))
	}

	@Test
	fun shouldSubtractLongFromSignal() {
		assertEquals(of(BW_4, 2UL), operation("A - B", of(BW_4, 3UL), 1L))
		assertEquals(of(BW_4, 13UL), operation("A - B", of(BW_4, 14UL), 1L))
	}

	@Test
	fun shouldMultiplySignals() {
		assertEquals(of(BW_4, 6UL), operation("A * B", of(BW_4, 3UL), of(BW_2, 2UL)))
		assertEquals(of(BW_4, 28UL), operation("A * B", of(BW_4, 14UL), of(BW_2, 2UL)))
	}

	@Test
	fun shouldMultiplyLongWithSignal() {
		assertEquals(of(BW_4, 6UL), operation("A * B", of(BW_4, 3UL), 2L))
		assertEquals(of(BW_4, 28UL), operation("A * B", of(BW_4, 14UL), 2L))
	}

	@Test
	fun shouldDivideSignals() {
		assertEquals(of(BW_4, 3UL), operation("A / B", of(BW_4, 6UL), of(BW_2, 2UL)))
		assertEquals(of(BW_8, 6UL), operation("A / B", of(BW_8, 24UL), of(BW_4, 4UL)))
	}

	@Test
	fun shouldDivideSignalByLong() {
		assertEquals(of(BW_4, 3UL), operation("A / B", of(BW_4, 6UL), 2L))
		assertEquals(of(BW_8, 6UL), operation("A / B", of(BW_8, 24UL), 4L))
	}

	private fun operation(statement: String, a: DigitalSignal, b: DigitalSignal): DigitalSignal {
		val parser = AntaresParser(AntaresLexer(statement), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("A", a)
		memory.preset("B", b)
		return interpreter.interpret() as DigitalSignal
	}

	private fun operation(statement: String, a: DigitalSignal, b: Long): DigitalSignal {
		val parser = AntaresParser(AntaresLexer(statement), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("A", a)
		memory.preset("B", b)
		return interpreter.interpret() as DigitalSignal
	}

	@Test
	fun shouldBeEqualToWord() {
		val parser = AntaresParser(AntaresLexer("A == B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		memory.preset("B", of(BW_4, 3UL))
		assertEquals(1L, interpreter.interpret())

		memory.preset("A", of(BW_4, 5UL))
		memory.preset("B", of(BW_4, 4UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldBeEqualToLong() {
		val parser = AntaresParser(AntaresLexer("A == 3"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		assertEquals(1L, interpreter.interpret())

		memory.preset("A", of(BW_4, 5UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldWordsOfDifferentWidthBeEqual() {
		val parser = AntaresParser(AntaresLexer("A == B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		memory.preset("B", of(BW_2, 3UL))
		assertEquals(1L, interpreter.interpret())

		memory.preset("A", of(BW_4, 5UL))
		memory.preset("B", of(BW_2, 2UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldDifferFromLong() {
		val parser = AntaresParser(AntaresLexer("A != 3"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		assertEquals(0L, interpreter.interpret())

		memory.preset("A", of(BW_4, 5UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldDifferFromWord() {
		val parser = AntaresParser(AntaresLexer("A != B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		memory.preset("B", of(BW_4, 3UL))
		assertEquals(0L, interpreter.interpret())

		memory.preset("A", of(BW_4, 5UL))
		memory.preset("B", of(BW_4, 4UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldModSignal() {
		val parser = AntaresParser(AntaresLexer("A % 2"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		assertEquals(of(BW_4, 1UL), interpreter.interpret())
	}

	@Test
	fun shouldBeGreaterThanSignal() {
		val parser = AntaresParser(AntaresLexer("A > B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		memory.preset("B", of(BW_4, 2UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldBeGreaterThanULong() {
		val parser = AntaresParser(AntaresLexer("A > 2"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldBeGreaterEqualThanSignal() {
		val parser = AntaresParser(AntaresLexer("A >= B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		memory.preset("B", of(BW_4, 2UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldBeGreaterEqualThanULong() {
		val parser = AntaresParser(AntaresLexer("A >= 2"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldBeSmallerThanSignal() {
		val parser = AntaresParser(AntaresLexer("A < B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		memory.preset("B", of(BW_4, 2UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldBeSmallerThanULong() {
		val parser = AntaresParser(AntaresLexer("A < 2"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldBeSmallerEqualThanSignal() {
		val parser = AntaresParser(AntaresLexer("A <= B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		memory.preset("B", of(BW_4, 2UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldBeSmallerEqualThanULong() {
		val parser = AntaresParser(AntaresLexer("A <= 2"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 3UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldEvaluateTrueSignalInIfCondition() {
		val parser = AntaresParser(AntaresLexer("""
			var A = 0
			if (B) {
				A = 1
			}
			A
		""".trimIndent()), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("B", of(BW_4, 1UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldEvaluateFalseSignalInIfCondition() {
		val parser = AntaresParser(AntaresLexer("""
			var A = 0
			if (B) {
				A = 1
			}
			A
		""".trimIndent()), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("B", of(BW_4, 0UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldEvaluateSignalInWhenCondition() {
		val parser = AntaresParser(AntaresLexer("""
			var A = 0
			when (B) {
				1 : A = 11
				2 : A = 22
				else : A = 99
			}
			A
		""".trimIndent()), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("B", of(BW_4, 2UL))
		assertEquals(22L, interpreter.interpret())
	}

	@Test
	fun shouldGetBitOfWordVariable() {
		val parser = AntaresParser(AntaresLexer("A@1"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 15UL))
		assertEquals(of(true), interpreter.interpret())
	}

	@Test
	fun shouldSetBitOfWordVariable() {
		val parser = AntaresParser(AntaresLexer("""
			A@1 = 1
		""".trimIndent()), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 0UL))
		assertEquals(of(BW_4, 2UL), interpreter.interpret())
	}

	@Test
	fun shouldGetBitOfWordInAssignment() {
		val parser = AntaresParser(AntaresLexer("""
			var B = A@1
			B
		""".trimIndent()), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 15UL))
		assertEquals(of(BW_1, 1UL), interpreter.interpret())
	}

	@Test
	fun shouldGetBitOfWordWithNumericVariableIndex() {
		val parser = AntaresParser(AntaresLexer("A@i"), null)

		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 15UL))
		memory.preset("i", 2L)
		assertEquals(of(BW_1, 1UL), interpreter.interpret())
	}

	@Test
	fun shouldGetBitOfWordWithSignalVariableIndex() {
		val parser = AntaresParser(AntaresLexer("A@i"), null)

		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 15UL))
		memory.preset("i", of(BW_4, 1UL))
		assertEquals(of(BW_1, 1UL), interpreter.interpret())
	}

	@Test
	fun shouldGetBitAsZeroBeyondBitWidth() {
		val parser = AntaresParser(AntaresLexer("A@7"), null)

		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 15UL))
		assertEquals(of(BW_1, 0UL), interpreter.interpret())
	}

	@Test
	fun shouldThrowDslErrorWhenAddingUndefinedSignals() {
		val parser = AntaresParser(AntaresLexer("A + B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_4, 1UL))
		memory.preset("B", Word.undefined(BW_4))

		assertFailsWith(DslError::class) {
			interpreter.interpret()
		}
	}

	@Test
	fun shouldLeftAssociateTypeInOperation() {
		val parser = AntaresParser(AntaresLexer("""
			2 * F1 + F0
		""".trimIndent()), null)

		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("F0", of(true))
		memory.preset("F1", of(true))
		assertEquals(3L, interpreter.interpret())
	}

	@Test
	fun shouldShiftLeftWithVarRightTerm() {
		val parser = AntaresParser(AntaresLexer("""
			1 << A
		""".trimIndent()), null)

		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_2, 2UL))
		assertEquals(4L, interpreter.interpret())
	}

	@Test
	fun shouldShiftRightWithVarRightTerm() {
		val parser = AntaresParser(AntaresLexer("""
			4 >> A
		""".trimIndent()), null)

		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", of(BW_2, 2UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldNotFailArithmeticsWithUndefinedInput() {
		val parser = AntaresParser(AntaresLexer("1 + A"), null)

		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", DigitalSignalFactory.undefined(BW_4))
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertEquals(1L, interpreter.interpret())
	}
}