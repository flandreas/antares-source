package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.AntaresTestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TruthTableServiceTest {

	private val service = TruthTableService()

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldCreateWithUserInput() {
		val truthTable = service.createWithUserInput("Test", "A,B", "O")

		assertEquals("Test", truthTable.name.value)
		assertEquals(2, truthTable.inputColumnCount)
		assertEquals(1, truthTable.outputColumnCount)
		assertTrue(truthTable.hasInputName("A"))
		assertTrue(truthTable.hasInputName("B"))
		assertTrue(truthTable.hasOutputName("O"))
	}

	@Test
	fun shouldRejectNegatedInputName() {
		assertFailsWith(IllegalArgumentException::class) {
			service.createWithUserInput("Test", "!A,B", "O")
		}
	}

	@Test
	fun shouldRejectIllegalInputName() {
		assertFailsWith(IllegalArgumentException::class) {
			service.createWithUserInput("Test", "A,3B", "O")
		}
	}

	@Test
	fun shouldSupportNegatedOutputName() {
		service.createWithUserInput("Test", "A,B", "!OUT")
		service.createWithUserInput("Test", "A,B", "!(OUT)")
	}

	@Test
	fun shouldRejectIllegalOutputName() {
		assertFailsWith(IllegalArgumentException::class) {
			service.createWithUserInput("Test", "A,B", "3X")
		}
	}
}