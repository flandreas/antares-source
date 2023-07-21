package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Translations
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Basic unit tests using [Int] values.*/
class InterpreterTest {

	@BeforeTest
	fun setup() {
		Translations.withAnyKey()
	}

	@Test
	fun shouldCalculateTerms() {
		assertEquals(84L, Interpreter("7*12").interpret())
		assertEquals(-24L, Interpreter("-3*8").interpret())
		assertEquals(3L, Interpreter("12/4").interpret())
		assertEquals(-8L, Interpreter("-8").interpret())
		assertEquals(10L, Interpreter("1 + 2 + 3 + 4").interpret())
		assertEquals(8L, Interpreter("2^3").interpret())
	}

	@Test
	fun shouldCalculateExpressions() {
		assertEquals(3L, Interpreter("1+2").interpret())
		assertEquals(8L, Interpreter("17 - 9").interpret())
		assertEquals(20L, Interpreter("(1 + 4) * (7 - 3)").interpret())
		assertEquals(12L, Interpreter("2 * (2 + 8 / (1 + 1))").interpret())
	}

	@Test
	fun shouldInterpretEmptyStatementList() {
		val result = Interpreter("").interpret()

		assertEquals(0L, result)
	}

	@Test
	fun shouldInterpretStatementList() {
		val result = Interpreter("""
			4 + 7
			5 * 10
		""".trimIndent()).interpret()

		assertEquals(50L, result)
	}

	@Test
	fun shouldNotNeedSemicolonForLastStatement() {
		val result = Interpreter("""
			4 + 7
			5 * 10
		""".trimIndent()).interpret()

		assertEquals(50L, result)
	}

	@Test
	fun shouldInterpretAssignments() {
		val result = Interpreter("""
			var a = 5
			var b = 7 * a
			b
		""".trimIndent()).interpret()

		assertEquals(35L, result)
	}

	@Test
	fun shouldInterpretBlocks() {
		val result = Interpreter("""
			var a = 5
			{
				var b = 7 * a
			}
		""".trimIndent()).interpret()

		assertEquals(35L, result)
	}

	@Test
	fun shouldInterpretDeclaration() {
		val result = Interpreter("""
			var a = 5
			var b = 2 * a
		""".trimIndent()).interpret()

		assertEquals(10L, result)
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
			var a = 5
			{
				a = 2
			}
			a
		""".trimIndent()).interpret()

		assertEquals(2L, result)
	}

	@Test
	fun shouldShadowEnclosedScope() {
		val result = Interpreter("""
			var a = 5
			{
				var a = 2
			}
			a
		""".trimIndent()).interpret()

		assertEquals(5L, result)
	}

	@Test
	fun shouldInterpretEqual() {
		assertEquals(0L, Interpreter("3 == 2").interpret())
		assertEquals(1L, Interpreter("34 == 34").interpret())
	}

	@Test
	fun shouldInterpretDiff() {
		assertEquals(1L, Interpreter("3 != 2").interpret())
		assertEquals(0L, Interpreter("34 != 34").interpret())
	}

	@Test
	fun shouldInterpretSmaller() {
		assertEquals(1L, Interpreter("2 < 3").interpret())
		assertEquals(0L, Interpreter("3 < 2").interpret())
	}

	@Test
	fun shouldInterpretGreater() {
		assertEquals(1L, Interpreter("3 > 2").interpret())
		assertEquals(0L, Interpreter("2 > 3").interpret())
	}

	@Test
	fun shouldInterpretSmallerEqual() {
		assertEquals(1L, Interpreter("2 <= 3").interpret())
		assertEquals(0L, Interpreter("3 <= 2").interpret())
		assertEquals(1L, Interpreter("3 <= 3").interpret())
	}

	@Test
	fun shouldInterpretGreaterEqual() {
		assertEquals(1L, Interpreter("3 >= 2").interpret())
		assertEquals(1L, Interpreter("3 >= 3").interpret())
		assertEquals(0L, Interpreter("2 >= 3").interpret())
	}

	@Test
	fun shouldExecuteThenForTrueIfCondition() {
		val result = Interpreter("""
			var a = 5
			var b = 17
			if (a == 5) {
				b = 42
			}
			b
		""".trimIndent()).interpret()

		assertEquals(42L, result)
	}

	@Test
	fun shouldNotExecuteThenForFalseIfCondition() {
		val result = Interpreter("""
			var a = 5
			var b = 17
			if (a == 4) {
				b = 42
			}
			b
		""".trimIndent()).interpret()

		assertEquals(17L, result)
	}

	@Test
	fun shouldExecuteElseStatementForFalseIfCondition() {
		val result = Interpreter("""
			var a = 5
			var b = 17
			if (a == 4) {
				b = 42
			} else {
				b = 9
			}
			b
		""".trimIndent()).interpret()

		assertEquals(9L, result)
	}

	@Test
	fun shouldExecuteStatementForTrueIfCondition() {
		val result = Interpreter("""
			var a = 5
			var b = 17
			if (a == 5) {
				b = 42
			} else {
				b = 9
			}
			b
		""".trimIndent()).interpret()

		assertEquals(42L, result)
	}

	@Test
	fun shouldCalculateAnd() {
		assertEquals(5L, Interpreter("5 and 5").interpret())
		assertEquals(0L, Interpreter("0 and 5").interpret())
	}

	@Test
	fun shouldCalculateOr() {
		assertEquals(3L, Interpreter("1 or 2").interpret())
		assertEquals(5L, Interpreter("0 or 5").interpret())
	}

	@Test
	fun shouldShiftLeft() {
		assertEquals(2L, Interpreter("1 << 1").interpret())
	}

	@Test
	fun shouldShiftRight() {
		assertEquals(1L, Interpreter("2 >> 1").interpret())
	}

	@Test
	fun shouldShiftLeftWithVarRightTerm() {
		val result = Interpreter("""
			var a = 2
			1 << a
		""".trimIndent()).interpret()

		assertEquals(4L, result)
	}

	@Test
	fun shouldInterpretMod() {
		assertEquals(1L, Interpreter("5 % 2").interpret())
		assertEquals(0L, Interpreter("6 % 2").interpret())
		assertEquals(3L, Interpreter("7 % 4").interpret())
	}

	@Test
	fun shouldExecuteIfStatementWithAndCondition() {
		val result = Interpreter("""
			var a = 5
			var b = 17
			if (a == 5 and b == 17) {
				b = 42
			}
			b
		""".trimIndent()).interpret()

		assertEquals(42L, result)
	}

	@Test
	fun shouldInterpretWhenStatement() {
		val result = Interpreter("""
			var a = 2
			var b = 0
			when (a) {
				1 : b = 11
				2 : b = 22
				3 : b = 33
				else : b = 99
			}
			b
		""".trimIndent()).interpret()

		assertEquals(22L, result)
	}

	@Test
	fun shouldInterpretWhenStatementByElse() {
		val result = Interpreter("""
			var a = 4
			var b = 0
			when (a) {
				1 : b = 11
				2 : b = 22
				3 : b = 33
				else : b = 99
			}
			b
		""".trimIndent()).interpret()

		assertEquals(99L, result)
	}

	@Test
	fun shouldInterpretForAscending() {
		val result = Interpreter("""
			var a = 0
			for (i in 1 to 3) {
				a = a + i
			}
			a
		""".trimIndent()).interpret()

		assertEquals(6L, result)
	}

	@Test
	fun shouldInterpretForDescending() {
		val result = Interpreter("""
			var a = 0
			for (i in 3 to 1) {
				a = a + i
			}
			a
		""".trimIndent()).interpret()

		assertEquals(6L, result)
	}

	@Test
	fun shouldCalculateNot() {
		// Result of signed integer calculation
		assertEquals(-3L, Interpreter("not 2").interpret())
	}

	@Test
	fun shouldInterpretAssocArray() {
		val result = Interpreter("""
			var a
			a[27] = 15
			a[28] = 11
			a[27]
		""".trimIndent()).interpret()

		assertEquals(15L, result)
	}

	@Test
	fun shouldUseExpressionAsArrayIndex() {
		val result = Interpreter("""
			var a
			a[1+1] = 42
			a[2]
		""".trimIndent()).interpret()

		assertEquals(42L, result)
	}

	@Test
	fun shouldInterpretAssocArrayInAssignment() {
		val result = Interpreter("""
			var a
			a[0] = 12
			a[1] = a[0]
			a[1]
		""".trimIndent()).interpret()

		assertEquals(12L, result)
	}

	@Test
	fun shouldStoreVariablesBetweenInterpretations() {
		val memory = Memory()
		val interpreter = Interpreter(DslParser(DslLexer("""
			store a
			var out = 0
			if (doStore) {
				a = 42
			}
			if (doLoad) {
				out = a
			}
			out
		""".trimIndent()), semanticAnalyser = null).parse(), memory)

		// Do store
		memory.preset("doStore", 1L)
		memory.preset("doLoad", 0L)
		assertEquals(0L, interpreter.interpret())

		// Use from store
		memory.preset("doStore", 0L)
		memory.preset("doLoad", 1L)
		assertEquals(42L, interpreter.interpret())
	}

	@Test
	fun shouldReturnFromIf() {
		val result = Interpreter("""
			if (1) {
				return 42
			}
			1
		""".trimIndent()).interpret()

		assertEquals(42L, result)
	}

	@Test
	fun shouldReturnFromFor() {
		val result = Interpreter("""
			var a = 0
			for (i in 1 to 10) {
				a = i
				return 99
			}
			a
		""".trimIndent()).interpret()

		assertEquals(99L, result)
	}

	@Test
	fun shouldSetArrayValuesInLoop() {
		val interpreter = Interpreter("""
			var a
			for (i in 1 to 3) {
				a[i] = 2 * i
			}
			a[3]
		""".trimIndent())

		val result = interpreter.interpret()

		assertEquals(6L, result)
	}

	@Test
	fun shouldInterpretFunctionCall() {
		val symbolTable = ScopedSymbolTable("global", 1, null)
		val function = ExternalFunction { (it[0] as Long) * (it[0] as Long) }
		symbolTable.define(ExternalFunctionSymbol("square", 1, function))

		val semanticAnalyser = SemanticAnalyser(symbolTable)
		val interpreter = Interpreter(DslParser(DslLexer("square(4)"), semanticAnalyser).parse())

		val result = interpreter.interpret()

		assertEquals(16L, result)
	}
}