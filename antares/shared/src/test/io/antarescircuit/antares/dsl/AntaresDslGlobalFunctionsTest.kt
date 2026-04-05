package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_8
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.dsl.DslSemanticAnalyser
import io.antarescircuit.jabbah.base.dsl.Memory
import io.antarescircuit.jabbah.base.dsl.Symbol
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresDslGlobalFunctionsTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldCalculateBitsInDigitalSignal() {
		val analyser = DslSemanticAnalyser(null)
		analyser.scope.define(Symbol("I"))
		val parser = AntaresParser(AntaresLexer("bits(I, 3, 2)"), analyser)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("I", DigitalSignalFactory.of(BW_8, 15))

		val result = interpreter.interpret()

		assertEquals(1L, result)
	}

	@Test
	fun shouldCalculateBitsInLong() {
		val analyser = DslSemanticAnalyser(null)
		analyser.scope.define(Symbol("I"))
		val parser = AntaresParser(AntaresLexer("bits(I, 3, 2)"), analyser)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("I", 15L)

		val result = interpreter.interpret()

		assertEquals(1L, result)
	}

	@Test
	fun shouldGateInputSignal() {
		val analyser = DslSemanticAnalyser(null)
		analyser.scope.define(Symbol("I"))
		val parser = AntaresParser(AntaresLexer("gated(I)"), analyser)
		val memory = Memory()
		val interpreter = AntaresInterpreter(parser.parse(), memory)
		memory.preset("I", DigitalSignalFactory.undefined(BW_8))

		val result = interpreter.interpret()

		assertEquals(DigitalSignalFactory.falseValue(BW_8), result)
	}
}