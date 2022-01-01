package ch.scorpion.antares.dsl

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.Symbol
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresDslGlobalFunctionsTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldCalculateBitsInDigitalSignal() {
		val analyser = SemanticAnalyser(null)
		analyser.scope.define(Symbol("I"))
		val parser = AntaresParser(AntaresLexer("bits(I, 3, 2)"), analyser)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("I", DigitalSignalFactory.of(BitWidth.BW_8, 15))

		val result = interpreter.interpret()

		assertEquals(1L, result)
	}

	@Test
	fun shouldCalculateBitsInLong() {
		val analyser = SemanticAnalyser(null)
		analyser.scope.define(Symbol("I"))
		val parser = AntaresParser(AntaresLexer("bits(I, 3, 2)"), analyser)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("I", 15L)

		val result = interpreter.interpret()

		assertEquals(1L, result)
	}
}