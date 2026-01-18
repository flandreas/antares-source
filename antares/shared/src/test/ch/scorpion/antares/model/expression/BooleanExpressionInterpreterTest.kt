package ch.scorpion.antares.model.expression

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.dsl.*
import kotlin.test.Test
import kotlin.test.assertEquals

class BooleanExpressionInterpreterTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldInterpretOrAndExpression() {
		val memory = Memory()
		val parser = BooleanExpressionParser(expectAssignment = false,"A * B' + A' * B")
		val interpreter = BooleanExpressionInterpreter(parser.parse(), memory)
		memory.preset("A", true)
		memory.preset("B", false)

		val result = interpreter.interpret()

		assertEquals(true, result)
	}

	@Test
	fun shouldInterpretTrueLiteral() {
		val memory = Memory()
		val parser = BooleanExpressionParser(expectAssignment = false,"A + 1")
		val interpreter = BooleanExpressionInterpreter(parser.parse(), memory)
		memory.preset("A", false)

		val result = interpreter.interpret()

		assertEquals(true, result)
	}

	@Test
	fun shouldInterpretFalseLiteral() {
		val memory = Memory()
		val parser = BooleanExpressionParser(expectAssignment = false,"A + 0")
		val interpreter = BooleanExpressionInterpreter(parser.parse(), memory)
		memory.preset("A", false)

		val result = interpreter.interpret()

		assertEquals(false, result)
	}

	@Test
	fun shouldInterpretAssignment() {
		val memory = Memory()
		val parser = BooleanExpressionParser(expectAssignment = true,"""
			X = A * B' + A' * B
			Y = A + B
		""".trimIndent())
		val interpreter = BooleanExpressionInterpreter(parser.parse(), memory)

		memory.preset("A", true)
		memory.preset("B", false)

		val result = interpreter.interpret()

		assertEquals(true, result)
		assertEquals(2, interpreter.assignedVariables.size)
		assertEquals("X", interpreter.assignedVariables[0])
		assertEquals("Y", interpreter.assignedVariables[1])
	}
}