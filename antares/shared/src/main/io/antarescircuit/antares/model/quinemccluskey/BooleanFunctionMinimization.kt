package io.antarescircuit.antares.model.quinemccluskey

typealias DNF = List<List<Literal>>

/** A product of sums: each inner list is a clause of literals being OR-ed together. */
typealias CNF = List<List<Literal>>

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

/**
 * Minimizes to a product of sums by minimizing the complement ([maxTerms], same [dontCares])
 * to a DNF and applying De Morgan: every complement term becomes a clause of negated literals.
 * An empty result means constant 1, a single empty clause means constant 0.
 */
fun minimizeToCNF(
	maxTerms: List<MinTerm>,
	dontCares: List<MinTerm> = emptyList(),
	n: Int
): CNF = minimizeToDNF(maxTerms, dontCares, n).map { term -> term.map { -it } }
