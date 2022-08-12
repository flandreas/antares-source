package ch.scorpion.antares.model.quinemccluskey

internal fun getAdditionalCoveringPrimeImplicants(
	primeImplicants: List<Implicant>,
	minTerms: List<MinTerm>
): List<Implicant> = when {
	minTerms.isEmpty() -> emptyList()
	else -> primeImplicants
		.powerSet()
		.filter { subset -> subset.flatMap { it.minTerms }.containsAll(minTerms) }
		.minWithOrNull(
			compareBy(
				{ subset -> subset.size },
				{ subset -> subset.sumOf { it.literals.size } }
			)
		)!!
}