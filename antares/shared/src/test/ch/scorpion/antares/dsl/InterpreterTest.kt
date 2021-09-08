package ch.scorpion.antares.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class InterpreterTest {

	@Test
	fun shouldCalculateTerms() {
		assertEquals(84, Interpreter("7*12").interpret())
		assertEquals(-24, Interpreter("-3*8").interpret())
		assertEquals(3, Interpreter("12/4").interpret())
		assertEquals(-8, Interpreter("-8").interpret())
		assertEquals(10, Interpreter("1 + 2 + 3 + 4").interpret())
	}

	@Test
	fun shouldCalculateExpressions() {
		assertEquals(3, Interpreter("1+2").interpret())
		assertEquals(8, Interpreter("17 - 9").interpret())
		assertEquals(20, Interpreter("(1 + 4) * (7 - 3)").interpret())
		assertEquals(12, Interpreter("2 * (2 + 8 / (1 + 1))").interpret())
	}
}