package io.antarescircuit.antares.model.quinemccluskey

typealias DNF = List<List<Literal>>

/**
 * Optimized Quine-McCluskey implementation using bitwise operations,
 * mask-partitioning, and exact branch-and-bound set cover.
 */
fun minimizeToDNF(
	minTerms: List<MinTerm>,
	dontCares: List<MinTerm> = emptyList(),
	n: Int
): DNF {
	if (minTerms.isEmpty()) return emptyList()

	val primeImplicants = getPrimeImplicants(minTerms, dontCares)
	val coveringPrimeImplicants = solveSetCover(primeImplicants, minTerms)

	return coveringPrimeImplicants.map { it.toLiterals(n) }
}