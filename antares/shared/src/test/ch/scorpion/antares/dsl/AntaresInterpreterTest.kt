package ch.scorpion.antares.dsl

import ch.scorpion.antares.AntaresTestRule
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresInterpreterTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldInterpretHexLiteral() {
		val result = AntaresInterpreter("a = 0xFF").interpret()
		assertEquals(255L, result)
	}
}