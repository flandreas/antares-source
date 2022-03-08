package ch.scorpion.antares.model.quinemcclusky

import ch.scorpion.antares.model.quinemccluskey.MinTerm
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

	@Test
	fun shouldMinimizeToConstantTrue() {
		val minTerms = listOf(
			"11"
		).map { it.toInt(2) }

		val dontCares = listOf(
			"00",
			"01",
			"10",
			//"11"
		).map { it.toInt(2) }

		val minimalDNF = minimizeToDNF(minTerms, dontCares, n = 2)

		// Contains a single empty list => "true"
		assertEquals(listOf(listOf()), minimalDNF)
	}

	@Test
	fun shouldMinimizeToConstantFalse() {
		val minTerms = listOf<MinTerm>()

		val dontCares = listOf(
			"00",
			"01",
			"10",
			//"11"
		).map { it.toInt(2) }

		val minimalDNF = minimizeToDNF(minTerms, dontCares, n = 2)

		// Contains nothing => "false"
		assertEquals(listOf(), minimalDNF)
	}
}