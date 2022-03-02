package ch.scorpion.antares.model.quinemcclusky

import ch.scorpion.antares.model.quinemccluskey.minimizeToDNF
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BooleanFunctionMinimizationTest {

	/** Example on https://en.wikipedia.org/wiki/Quine–McCluskey_algorithm.*/
	@Test
	fun shouldMinimize() {
		val minTerms = listOf(
			"1011",
			"0111",
			"0101",
			"0100",
			"0011",
			"0000"
		).map { it.toInt(2) }

		val dontCares = listOf(
			"0110",
			"0001"
		).map { it.toInt(2) }

		val minimalDNF = minimizeToDNF(minTerms, dontCares, n = 4)

		assertEquals(3, minimalDNF.size)
		assertTrue(minimalDNF.contains(listOf(-2, 3, 4))) // BC'D'
		assertTrue(minimalDNF.contains(listOf(-1, 2))) // AB'
		assertTrue(minimalDNF.contains(listOf(-1, -3))) // AC
	}
}