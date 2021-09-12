package ch.scorpion.antares.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresInterpreterTest {

	@Test
	fun shouldInterpretHexLiteral() {
		val result = AntaresInterpreter("a = 0xFF").interpret()
		assertEquals(255L, result)
	}
}