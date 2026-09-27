package io.antarescircuit.antares.dsl

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_8
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.dsl.DslSemanticAnalyser
import io.antarescircuit.jabbah.base.dsl.Memory
import io.antarescircuit.jabbah.base.dsl.Symbol
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphFunctionContext
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
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

	@Test
	fun shouldYieldTime() {
		val parser = AntaresParser("time()")
		val signalHandler = mock<SignalHandler>()
		every { signalHandler.executionTime } returns 999
		val context = SubGraphFunctionContext(mock(), mock(), signalHandler)
		val interpreter = AntaresInterpreter(parser.parse())
		val result = interpreter.interpret(context)

		assertEquals(999L, result)
	}

	@Test
	fun shouldYieldPropDelay() {
		val parser = AntaresParser("propDelay()")
		val actor = SubGraphVerticeRef()
		actor.propagationDelay = LongValueImpl(1000L)
		val context = SubGraphFunctionContext(mock(), actor, mock())
		val interpreter = AntaresInterpreter(parser.parse())
		val result = interpreter.interpret(context)

		assertEquals(1000L, result)
	}
}