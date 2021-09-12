package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [Interpreter] using [DigitalSignal] values.*/
class InterpreterSignalTest {

	@Test
	fun presetGlobalVariableWithSemanticAnalysis() {
		val program = """
			// a is preset by environment to 5
			b = a + 1
		""".trimIndent()

		val analyser = SemanticAnalyser()
		analyser.scope.define(Symbol("a"))

		val parser = Parser(Lexer(program), analyser)

		val interpreter = Interpreter(parser.parse())
		interpreter.memory.preset("a", 5L)

		val result = interpreter.interpret()

		assertEquals(6, result)
	}

	@Test
	fun presetGlobalVariableWithoutSemanticAnalysis() {
		val program = """
			// a is preset by environment to 5
			b = a + 1
		""".trimIndent()

		val parser = Parser(Lexer(program), EmptyHierarchyVisitor())

		val interpreter = Interpreter(parser.parse())
		interpreter.memory.preset("a", 5L)

		val result = interpreter.interpret()

		assertEquals(6, result)
	}

}