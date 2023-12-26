package ch.scorpion.antares.dsl

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.gate.CurrentUndefinedGateInputBehavior
import ch.scorpion.antares.model.gate.UndefinedGateInputBehavior
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_1
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_2
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_4
import ch.scorpion.antares.model.signal.DigitalLiteral
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.Word.Companion.of
import ch.scorpion.jabbah.base.dsl.DslSemanticAnalyser
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.dsl.RuntimeError
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

		val analyser = DslSemanticAnalyser(null)
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
	fun shouldAdd() {
		assertEquals(of(BW_4, 3UL), operation("A + B", of(BW_4, 2UL), of(BW_2, 1UL)))
		assertEquals(of(BW_4, 3UL), operation("A + B", of(BW_4, 2UL), 1L))
		assertEquals(3L, operation("A + B", 1L, of(BW_4, 2UL)))
		assertEquals(4.5F, AntaresInterpreter("3.5 + 1").interpret())
	}

	@Test
	fun shouldSubtract() {
		assertEquals(of(BW_4, 1UL), operation("A - B", of(BW_4, 2UL), of(BW_2, 1UL)))
		assertEquals(of(BW_4, 1UL), operation("A - B", of(BW_4, 2UL), 1L))
		assertEquals(2L, operation("A - B", 4L, of(BW_4, 2UL)))
		assertEquals(2.5F, AntaresInterpreter("3.5 - 1").interpret())
	}

	@Test
	fun shouldMultiply() {
		assertEquals(of(BW_4, 6UL), operation("A * B", of(BW_4, 2UL), of(BW_2, 3UL)))
		assertEquals(of(BW_4, 12UL), operation("A * B", of(BW_4, 3UL), 4L))
		assertEquals(8L, operation("A * B", 4L, of(BW_4, 2UL)))
		assertEquals(7.0F, AntaresInterpreter("3.5 * 2").interpret())
	}

	@Test
	fun shouldDivide() {
		assertEquals(of(BW_4, 4UL), operation("A / B", of(BW_4, 12UL), of(BW_2, 3UL)))
		assertEquals(of(BW_4, 3UL), operation("A / B", of(BW_4, 12UL), 4L))
		assertEquals(2L, operation("A / B", 4L, of(BW_4, 2UL)))
		assertEquals(3.5F, AntaresInterpreter("7.0 / 2").interpret())
	}

	@Test
	fun shouldPower() {
		assertEquals(of(BW_4, 8UL), operation("A^B", of(BW_4, 2UL), of(BW_2, 3UL)))
		assertEquals(of(BW_4, 8UL), operation("A^B", of(BW_4, 2UL), 3L))
		assertEquals(8L, operation("A^B", 2L, of(BW_4, 3UL)))
		assertEquals(49.0F, AntaresInterpreter("7.0^2").interpret())
	}

	@Test
	fun shouldBeEqual() {
		assertEquals(1L, operation("A == B", of(BW_4, 2UL), of(BW_2, 2UL)))
		assertEquals(1L, operation("A == B", of(BW_4, 2UL), 2L))
		assertEquals(1L, operation("A == B", 3L, of(BW_4, 3UL)))
		assertEquals(1L, AntaresInterpreter("7.0 == 7").interpret())
	}

	@Test
	fun shouldNotBeEqual() {
		assertEquals(0L, operation("A == B", of(BW_4, 2UL), of(BW_2, 3UL)))
		assertEquals(0L, operation("A == B", of(BW_4, 2UL), 3L))
		assertEquals(0L, operation("A == B", 3L, of(BW_4, 4UL)))
		assertEquals(0L, AntaresInterpreter("7.1 == 7").interpret())
	}

	@Test
	fun shouldBeDifferent() {
		assertEquals(1L, operation("A != B", of(BW_4, 2UL), of(BW_2, 3UL)))
		assertEquals(1L, operation("A != B", of(BW_4, 2UL), 3L))
		assertEquals(1L, operation("A != B", 3L, of(BW_4, 4UL)))
		assertEquals(1L, AntaresInterpreter("7.0 != 8").interpret())
	}

	@Test
	fun shouldNotDifferent() {
		assertEquals(0L, operation("A != B", of(BW_4, 2UL), of(BW_2, 2UL)))
		assertEquals(0L, operation("A != B", of(BW_4, 2UL), 2L))
		assertEquals(0L, operation("A != B", 3L, of(BW_4, 3UL)))
		assertEquals(0L, AntaresInterpreter("7.0 != 7").interpret())
	}

	@Test
	fun shouldBeSmaller() {
		assertEquals(1L, operation("A < B", of(BW_4, 2UL), of(BW_2, 3UL)))
		assertEquals(1L, operation("A < B", of(BW_4, 2UL), 3L))
		assertEquals(1L, operation("A < B", 3L, of(BW_4, 4UL)))
		assertEquals(1L, AntaresInterpreter("7.0 < 8").interpret())
	}

	@Test
	fun shouldBeGreater() {
		assertEquals(1L, operation("A > B", of(BW_4, 3UL), of(BW_2, 2UL)))
		assertEquals(1L, operation("A > B", of(BW_4, 3UL), 2L))
		assertEquals(1L, operation("A > B", 4L, of(BW_4, 3UL)))
		assertEquals(1L, AntaresInterpreter("7.0 > 6").interpret())
	}

	@Test
	fun shouldShiftLeft() {
		assertEquals(of(BW_4, 2UL), operation("A << B", of(BW_4, 1UL), of(BW_2, 1UL)))
		assertEquals(of(BW_4, 2UL), operation("A << B", of(BW_4, 1UL), 1L))
		assertEquals(2L, operation("A << B", 1L, of(BW_4, 1UL)))
		assertEquals(2L, AntaresInterpreter("1 << 1").interpret())
		assertFailsWith(RuntimeError::class) {
			AntaresInterpreter("1.0 << 2").interpret()
		}
	}

	@Test
	fun shouldShiftRight() {
		assertEquals(of(BW_4, 1UL), operation("A >> B", of(BW_4, 2UL), of(BW_2, 1UL)))
		assertEquals(of(BW_4, 1UL), operation("A >> B", of(BW_4, 2UL), 1L))
		assertEquals(1L, operation("A >> B", 2L, of(BW_4, 1UL)))
		assertEquals(1L, AntaresInterpreter("2 >> 1").interpret())
		assertFailsWith(RuntimeError::class) {
			AntaresInterpreter("1.0 >> 2").interpret()
		}
	}

	@Test
	fun shouldCalculateAnd() {
		assertEquals(of(BW_4, 2UL), operation("A and B", of(BW_4, 3UL), of(BW_2, 2UL)))
		assertEquals(of(BW_4, 2UL), operation("A and B", of(BW_4, 3UL), 2L))
		assertEquals(2L, operation("A and B", 2L, of(BW_4, 3UL)))
		assertEquals(2L, AntaresInterpreter("3 and 2").interpret())
		assertFailsWith(RuntimeError::class) {
			AntaresInterpreter("7.0 and 2").interpret()
		}
	}

	@Test
	fun shouldCalculateOr() {
		assertEquals(of(BW_4, 3UL), operation("A or B", of(BW_4, 2UL), of(BW_2, 1UL)))
		assertEquals(of(BW_4, 3UL), operation("A or B", of(BW_4, 2UL), 1L))
		assertEquals(3L, operation("A or B", 2L, of(BW_4, 1UL)))
		assertEquals(3L, AntaresInterpreter("2 or 1").interpret())
		assertFailsWith(RuntimeError::class) {
			AntaresInterpreter("7.0 or 2").interpret()
		}
	}

	@Test
	fun shouldCalculateMod() {
		assertEquals(of(BW_4, 1UL), operation("A % B", of(BW_4, 5UL), of(BW_2, 2UL)))
		assertEquals(of(BW_4, 1UL), operation("A % B", of(BW_4, 5UL), 2L))
		assertEquals(1L, operation("A % B", 5L, of(BW_4, 2UL)))
		assertEquals(1L, AntaresInterpreter("5 % 2").interpret())
		assertFailsWith(RuntimeError::class) {
			AntaresInterpreter("5.0 % 2").interpret()
		}
	}

	@Test
	fun shouldCalculateNot() {
		assertEquals(of(BW_4, 12UL), operation("not A", of(BW_4, 3UL), null as DigitalSignal?))
		// Result of signed integer calculation
		assertEquals(-4L, operation("not A", 3L))
	}

	@Test
	fun shouldCalculatePlus() {
		assertEquals(of(BW_4, 3UL), operation("+A", of(BW_4, 3UL), null as DigitalSignal?))
		assertEquals(3L, operation("+A", 3L))
	}

	@Test
	fun shouldCalculateMinus() {
		assertEquals(-3L, operation("-A", 3L))
		assertFailsWith(RuntimeError::class) {
			operation("-A", of(BW_4, 3UL), null as DigitalSignal?)
		}
	}

	private fun operation(statement: String, a: DigitalSignal, b: DigitalSignal? = null): Any {
		val parser = AntaresParser(AntaresLexer(statement), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("A", a)
		b?.let { memory.preset("B", it) }
		return interpreter.interpret()
	}

	private fun operation(statement: String, a: DigitalSignal, b: Long? = null): Any {
		val parser = AntaresParser(AntaresLexer(statement), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("A", a)
		b?.let { memory.preset("B", it) }
		return interpreter.interpret()
	}

	private fun operation(statement: String, a: Long, b: DigitalSignal? = null): Any {
		val parser = AntaresParser(AntaresLexer(statement), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("A", a)
		b?.let { memory.preset("B", it) }
		return interpreter.interpret()
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

	@Test
	fun shouldCalculate4BitDeMultiplexer() {
		val parser = AntaresParser(AntaresLexer("O0 = I and ((S0 + 2*S1) == 0)"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("O0", DigitalSignalFactory.of(true))
		memory.preset("I", DigitalSignalFactory.of(true))
		memory.preset("S0", DigitalSignalFactory.of(true))
		memory.preset("S1", DigitalSignalFactory.of(true))

		val result = interpreter.interpret()

		assertEquals(DigitalSignalFactory.of(false), result)
	}

	@Test
	fun shouldCalculate7SegmentDecoder() {
		val parser = AntaresParser(AntaresLexer("""
			var 'a..g'
			var input = 8*D + 4*C + 2*B + A
			when (input) {
				1 : 'a..g' = 0bZ0111111
				2 : 'a..g' = 0bZ0000110
				else : 'a..g' = 0bZ0000001
			}
		""".trimIndent()), null)

		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("A", DigitalSignalFactory.of(false))
		memory.preset("B", DigitalSignalFactory.of(true))
		memory.preset("C", DigitalSignalFactory.of(false))
		memory.preset("D", DigitalSignalFactory.of(false))

		interpreter.interpret(keepMemory = true)

		assertEquals(DigitalLiteral.parseBinary("Z0000110"), memory.getValue("a..g"))
	}
}