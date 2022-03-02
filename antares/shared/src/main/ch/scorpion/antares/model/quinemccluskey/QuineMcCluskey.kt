package ch.scorpion.antares.model.quinemccluskey

internal fun getPrimeImplicants(
	minTerms: List<MinTerm>,
	dontCares: List<MinTerm> = emptyList(),
	n: Int
): List<Implicant> {
	require(n >= 1)

	val initialGroups: List<MutableSet<Implicant>> = List(n + 1) { mutableSetOf() }
	for (m in minTerms + dontCares) {
		val numberOf1s = m.toString(2).count { it == '1' }
		initialGroups[numberOf1s].add(Implicant.minTerm(m, n = n))
	}
	var groups: List<Set<Implicant>> = initialGroups
	val primeImplicants: MutableSet<Implicant> = mutableSetOf()

	while (groups.isNotEmpty()) {
		val newGroups = mutableListOf<Set<Implicant>>()

		for ((group1, group2) in groups.zipWithNext()) {
			val newGroup = mutableSetOf<Implicant>()
			for (term1 in group1)
				for (term2 in group2) {
					term1.combine(term2)?.let { combined ->
						term1.mark()
						term2.mark()
						newGroup.add(combined)
					}
				}
			newGroups.add(newGroup)
		}

		groups.flatten().filter { !it.marked }.toCollection(primeImplicants)
		groups = newGroups
	}

	return primeImplicants.toList()
}

internal fun getEssentialPrimeImplicants(
	primeImplicants: List<Implicant>,
	minTerms: List<MinTerm>
): List<Implicant> {
	val essentialPrimeImplicants: MutableList<Implicant> = mutableListOf()
	val pis: MutableList<Implicant> = primeImplicants.toMutableList()
	val ms: MutableList<MinTerm> = minTerms.toMutableList()

	while (pis.isNotEmpty() && ms.isNotEmpty()) {
		// Extract EPIs from the prime implicant chart
		val epis = getEssentialPrimeImplicantsStep(pis, ms)
		if (epis.isEmpty()) break
		essentialPrimeImplicants += epis

		// Reduce the prime implicant chart
		pis -= epis.toSet()
		ms -= epis.flatMap { it.minTerms }.toSet()
		// TODO: eliminate redundant prime implicants
	}

	return essentialPrimeImplicants
}

private fun getEssentialPrimeImplicantsStep(
	primeImplicants: List<Implicant>,
	minterms: List<MinTerm>
): List<Implicant> =
	minterms
		.map { m -> primeImplicants.filter { m in it.minTerms } }
		.filter { it.size == 1 }
		.flatten()
		.distinct()