package ch.scorpion.antares.dsl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

	@Test
	fun shouldInterpretEmptyStatementList() {
		val result = Interpreter("").interpret()

		assertEquals(0, result)
	}

	@Test
	fun shouldInterpretStatementList() {
		val result = Interpreter("""
			4 + 7
			5 * 10
		""".trimIndent()).interpret()

		assertEquals(50, result)
	}

	@Test
	fun shouldNotNeedSemicolonForLastStatement() {
		val result = Interpreter("""
			4 + 7
			5 * 10
		""".trimIndent()).interpret()

		assertEquals(50, result)
	}

	@Test
	fun shouldInterpretAssignments() {
		val result = Interpreter("""
			a = 5
			b = 7 * a
			b
		""".trimIndent()).interpret()

		assertEquals(35, result)
	}

	@Test
	fun shouldInterpretBlocks() {
		val result = Interpreter("""
			a = 5
			{
				b = 7 * a
			}
		""".trimIndent()).interpret()

		assertEquals(35, result)
	}

	@Test
	fun shouldInterpretDeclaration() {
		val result = Interpreter("""
			var a = 5
			b = 2 * a
		""".trimIndent()).interpret()

		assertEquals(10, result)
	}

	@Test
	fun shouldNotRedeclare() {
		assertFailsWith(SemanticError::class) {
			try {
				Interpreter("""
				var a = 5
				var a = 6
			""".trimIndent()).interpret()
			} catch (e: Throwable) {
				throw e
			}
		}
	}

	@Test
	fun shouldAccessEnclosedScope() {
		val result = Interpreter("""
			a = 5
			{
				a = 2
			}
			a
		""".trimIndent()).interpret()

		assertEquals(2, result)
	}

	@Test
	fun shouldShadowEnclosedScope() {
		val result = Interpreter("""
			a = 5
			{
				var a = 2
			}
			a
		""".trimIndent()).interpret()

		assertEquals(5, result)
	}

	@Test
	fun shouldInterpretEqual() {
		assertEquals(0, Interpreter("3 == 2").interpret())
		assertEquals(1, Interpreter("34 == 34").interpret())
	}

	@Test
	fun shouldInterpretDiff() {
		assertEquals(1, Interpreter("3 != 2").interpret())
		assertEquals(0, Interpreter("34 != 34").interpret())
	}

	@Test
	fun shouldInterpretSmaller() {
		assertEquals(1, Interpreter("2 < 3").interpret())
		assertEquals(0, Interpreter("3 < 2").interpret())
	}

	@Test
	fun shouldInterpretGreater() {
		assertEquals(1, Interpreter("3 > 2").interpret())
		assertEquals(0, Interpreter("2 > 3").interpret())
	}

	@Test
	fun shouldInterpretSmallerEqual() {
		assertEquals(1, Interpreter("2 <= 3").interpret())
		assertEquals(0, Interpreter("3 <= 2").interpret())
		assertEquals(1, Interpreter("3 <= 3").interpret())
	}

	@Test
	fun shouldInterpretGreaterEqual() {
		assertEquals(1, Interpreter("3 >= 2").interpret())
		assertEquals(1, Interpreter("3 >= 3").interpret())
		assertEquals(0, Interpreter("2 >= 3").interpret())
	}

	@Test
	fun shouldExecuteThenForTrueIfCondition() {
		val result = Interpreter("""
			a = 5
			b = 17
			if (a == 5) {
				b = 42
			}
			b
		""".trimIndent()).interpret()

		assertEquals(42, result)
	}

	@Test
	fun shouldNotExecuteThenForFalseIfCondition() {
		val result = Interpreter("""
			a = 5
			b = 17
			if (a == 4) {
				b = 42
			}
			b
		""".trimIndent()).interpret()

		assertEquals(17, result)
	}

	@Test
	fun shouldExecuteElseStatementForFalseIfCondition() {
		val result = Interpreter("""
			a = 5
			b = 17
			if (a == 4) {
				b = 42
			} else {
				b = 9
			}
			b
		""".trimIndent()).interpret()

		assertEquals(9, result)
	}

	@Test
	fun shouldExecuteStatementForTrueIfCondition() {
		val result = Interpreter("""
			a = 5
			b = 17
			if (a == 5) {
				b = 42
			} else {
				b = 9
			}
			b
		""".trimIndent()).interpret()

		assertEquals(42, result)
	}

	@Test
	fun shouldCalculateAnd() {
		assertEquals(5, Interpreter("5 and 5").interpret())
		assertEquals(0, Interpreter("0 and 5").interpret())
	}

	@Test
	fun shouldCalculateOr() {
		assertEquals(3, Interpreter("1 or 2").interpret())
		assertEquals(5, Interpreter("0 or 5").interpret())
	}

	@Test
	fun shouldShiftLeft() {
		assertEquals(2, Interpreter("1 << 1").interpret())
	}

	@Test
	fun shouldShiftRight() {
		assertEquals(1, Interpreter("2 >> 1").interpret())
	}

	@Test
	fun shouldInterpretMod() {
		assertEquals(1, Interpreter("5 % 2").interpret())
		assertEquals(0, Interpreter("6 % 2").interpret())
		assertEquals(3, Interpreter("7 % 4").interpret())
	}

	@Test
	fun shouldExecuteIfStatementWithAndCondition() {
		val result = Interpreter("""
			a = 5
			b = 17
			if (a == 5 and b == 17) {
				b = 42
			}
			b
		""".trimIndent()).interpret()

		assertEquals(42, result)
	}

	@Test
	fun shouldCalculateNot() {
		// Result of signed integer calculation
		assertEquals(-3, Interpreter("not 2").interpret())
	}
}