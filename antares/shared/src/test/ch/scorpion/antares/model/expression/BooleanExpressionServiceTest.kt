package ch.scorpion.antares.model.expression

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit.False
import ch.scorpion.antares.model.signal.Bit.True
import ch.scorpion.jabbah.base.dsl.SemanticError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BooleanExpressionServiceTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val service = BooleanExpressionService()

	@Test
	fun shouldParseExpressions() {
		val expressions = """
			X = A * B' + A' * B
			Y = B + 1
		""".trimIndent()

		val result = service.parseExpressions(expressions, BooleanExpressionNotation.ARITHMETIC)

		assertEquals(2, result.inputNames.size)
		assertTrue(result.inputNames.contains("A"))
		assertTrue(result.inputNames.contains("B"))
		assertEquals(2, result.outputs.size)
		assertTrue(result.outputs.keys.contains("X"))
		assertTrue(result.outputs.keys.contains("Y"))
	}

	@Test
	fun shouldRejectOutputAsInput() {
		val expressions = """
			X = A * B' + A' * Y
			Y = B + 1
		""".trimIndent()

		assertFailsWith(SemanticError::class) {
			service.parseExpressions(expressions, BooleanExpressionNotation.ARITHMETIC)
		}
	}

	@Test
	fun shouldRejectOutputAsInput2() {
		val expressions = """
			X = A * B' + A' * B
			Y = B + X
		""".trimIndent()

		assertFailsWith(SemanticError::class) {
			service.parseExpressions(expressions, BooleanExpressionNotation.ARITHMETIC)
		}
	}

	@Test
	fun shouldCalculateOutputColumns() {
		val expressions = """
			X = A * B' + A' * B
			Y = B'
		""".trimIndent()
		val result = service.parseExpressions(expressions, BooleanExpressionNotation.ARITHMETIC)

		val truthTable = service.createTruthTable(result)

		assertEquals(4, truthTable.rowsCount)
		assertEquals(2, truthTable.inputColumnCount)
		assertEquals(2, truthTable.outputColumnCount)
		assertEquals(listOf(False, False, True, True), truthTable.getColumnValues(0))
		assertEquals(listOf(False, True, False, True), truthTable.getColumnValues(1))
		assertEquals(listOf(False, True, True, False), truthTable.getColumnValues(2))
		assertEquals(listOf(True, False, True, False), truthTable.getColumnValues(3))
	}
}