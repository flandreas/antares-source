package io.antarescircuit.antares.model.quinemcclusky

import io.antarescircuit.antares.model.quinemccluskey.MinTerm
import io.antarescircuit.antares.model.quinemccluskey.minimizeToCNF
import io.antarescircuit.antares.model.quinemccluskey.minimizeToDNF
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

	@Test
	fun shouldMinimizeToCnfByNegatingTheMinimizedComplement() {
		val maxTerms = listOf("11").map { it.toInt(2) }
		val dontCares = listOf("10").map { it.toInt(2) }

		// Complement minimizes to the single literal 1, so the only clause is its negation
		assertEquals(listOf(listOf(-1)), minimizeToCNF(maxTerms, dontCares, n = 2))
		assertEquals(listOf(listOf(-1, -2)), minimizeToCNF(maxTerms, n = 2))
	}

	@Test
	fun shouldMinimizeToCnfConstants() {
		// No zero rows => no clause => "true"
		assertEquals(listOf(), minimizeToCNF(listOf(), n = 2))
		// Only zero rows => a single empty clause => "false"
		assertEquals(listOf(listOf()), minimizeToCNF(listOf(0, 1, 2, 3), n = 2))
	}
}
