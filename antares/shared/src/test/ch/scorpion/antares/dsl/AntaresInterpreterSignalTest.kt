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
}