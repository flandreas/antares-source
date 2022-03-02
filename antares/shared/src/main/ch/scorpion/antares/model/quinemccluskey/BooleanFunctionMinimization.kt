package ch.scorpion.antares.model.quinemccluskey

/**
 * Source: https://github.com/Lipen/kotlin-quine-mccluskey.
 * Copies because GitHub project doesn't support Kotlin multiplatform code.
 */
fun minimizeToDNF(
	minTerms: List<MinTerm>,
	dontCares: List<MinTerm> = emptyList(),
	n: Int
): List<List<Literal>> {
	val primeImplicants = getPrimeImplicants(minTerms, dontCares, n)
	val essentialPrimeImplicants = getEssentialPrimeImplicants(primeImplicants, minTerms)
	val uncoveredMinTerms = minTerms - essentialPrimeImplicants.flatMap { it.minTerms }.toSet()
	val additionalPrimeImplicants =
		getAdditionalCoveringPrimeImplicants(
			primeImplicants = primeImplicants - essentialPrimeImplicants.toSet(),
			minTerms = uncoveredMinTerms
		)
	val coveringPrimeImplicants = essentialPrimeImplicants + additionalPrimeImplicants

	return coveringPrimeImplicants.map { it.literals }
}