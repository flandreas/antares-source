package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.Symbol
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [AntaresInterpreter] using [DigitalSignal] values.*/
class AntaresInterpreterSignalTest {

	@Test
	fun presetGlobalVariableWithSemanticAnalysis() {
		val program = """
			// a is preset by environment to 5
			b = a + 1
		""".trimIndent()

		val analyser = SemanticAnalyser()
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
			b = a + 1
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
			O = not I
		""".trimIndent()

		val parser = AntaresParser(AntaresLexer(program), null)

		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("I", Word.of(BitWidth.BW_4, 2UL))

		val result = interpreter.interpret()

		assertEquals(Word.of(BitWidth.BW_4, 13UL), result)
	}

	@Test
	fun shouldAddSignals() {
		val parser = AntaresParser(AntaresLexer("O = A + B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("A", Word.of(BitWidth.BW_4, 2UL))
		memory.preset("B", Word.of(BitWidth.BW_2, 1UL))

		val result = interpreter.interpret()

		assertEquals(Word.of(BitWidth.BW_4, 3UL), result)
	}

	@Test
	fun shouldAddLongToSignal() {
		val parser = AntaresParser(AntaresLexer("O = A + 3"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("A", Word.of(BitWidth.BW_4, 2UL))

		val result = interpreter.interpret()

		assertEquals(Word.of(BitWidth.BW_4, 5UL), result)
	}

	@Test
	fun shouldBeEqualToWord() {
		val parser = AntaresParser(AntaresLexer("A == B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		memory.preset("B", Word.of(BitWidth.BW_4, 3UL))
		assertEquals(1L, interpreter.interpret())

		memory.preset("A", Word.of(BitWidth.BW_4, 5UL))
		memory.preset("B", Word.of(BitWidth.BW_4, 4UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldBeEqualToLong() {
		val parser = AntaresParser(AntaresLexer("A == 3"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		assertEquals(1L, interpreter.interpret())

		memory.preset("A", Word.of(BitWidth.BW_4, 5UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldDifferFromLong() {
		val parser = AntaresParser(AntaresLexer("A != 3"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		assertEquals(0L, interpreter.interpret())

		memory.preset("A", Word.of(BitWidth.BW_4, 5UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldDifferFromWord() {
		val parser = AntaresParser(AntaresLexer("A != B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		memory.preset("B", Word.of(BitWidth.BW_4, 3UL))
		assertEquals(0L, interpreter.interpret())

		memory.preset("A", Word.of(BitWidth.BW_4, 5UL))
		memory.preset("B", Word.of(BitWidth.BW_4, 4UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldModSignal() {
		val parser = AntaresParser(AntaresLexer("A % 2"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		assertEquals(Word.of(BitWidth.BW_4, 1UL), interpreter.interpret())
	}

	@Test
	fun shouldBeGreaterThanSignal() {
		val parser = AntaresParser(AntaresLexer("A > B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		memory.preset("B", Word.of(BitWidth.BW_4, 2UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldBeGreaterThanULong() {
		val parser = AntaresParser(AntaresLexer("A > 2"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldBeGreaterEqualThanSignal() {
		val parser = AntaresParser(AntaresLexer("A >= B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		memory.preset("B", Word.of(BitWidth.BW_4, 2UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldBeGreaterEqualThanULong() {
		val parser = AntaresParser(AntaresLexer("A >= 2"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldBeSmallerThanSignal() {
		val parser = AntaresParser(AntaresLexer("A < B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		memory.preset("B", Word.of(BitWidth.BW_4, 2UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldBeSmallerThanULong() {
		val parser = AntaresParser(AntaresLexer("A < 2"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldBeSmallerEqualThanSignal() {
		val parser = AntaresParser(AntaresLexer("A <= B"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		memory.preset("B", Word.of(BitWidth.BW_4, 2UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldBeSmallerEqualThanULong() {
		val parser = AntaresParser(AntaresLexer("A <= 2"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 3UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldEvaluateTrueSignalInIfCondition() {
		val parser = AntaresParser(AntaresLexer("""
			A = 0
			if (B) {
				A = 1
			}
			A
		""".trimIndent()), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("B", Word.of(BitWidth.BW_4, 1UL))
		assertEquals(1L, interpreter.interpret())
	}

	@Test
	fun shouldEvaluateFalseSignalInIfCondition() {
		val parser = AntaresParser(AntaresLexer("""
			A = 0
			if (B) {
				A = 1
			}
			A
		""".trimIndent()), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("B", Word.of(BitWidth.BW_4, 0UL))
		assertEquals(0L, interpreter.interpret())
	}

	@Test
	fun shouldEvaluateSignalInWhenCondition() {
		val parser = AntaresParser(AntaresLexer("""
			A = 0
			when (B) {
				1 : A = 11
				2 : A = 22
				else : A = 99
			}
			A
		""".trimIndent()), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("B", Word.of(BitWidth.BW_4, 2UL))
		assertEquals(22L, interpreter.interpret())
	}

	@Test
	fun shouldGetBitOfWordVariable() {
		val parser = AntaresParser(AntaresLexer("A@1"), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 15UL))
		assertEquals(Word.of(true), interpreter.interpret())
	}

	@Test
	fun shouldSetBitOfWordVariable() {
		val parser = AntaresParser(AntaresLexer("""
			A@1 = 1
		""".trimIndent()), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 0UL))
		assertEquals(Word.of(BitWidth.BW_4, 2UL), interpreter.interpret())
	}

	@Test
	fun shouldGetBitOfWordInAssignment() {
		val parser = AntaresParser(AntaresLexer("""
			B = A@1
			B
		""".trimIndent()), null)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)

		memory.preset("A", Word.of(BitWidth.BW_4, 15UL))
		assertEquals(Word.of(BitWidth.BW_1, 1UL), interpreter.interpret())
	}
}