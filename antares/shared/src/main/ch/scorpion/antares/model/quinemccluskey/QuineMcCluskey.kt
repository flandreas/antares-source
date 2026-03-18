package ch.scorpion.antares.model.quinemccluskey

internal fun getPrimeImplicants(
	minTerms: List<MinTerm>,
	dontCares: List<MinTerm> = emptyList()
): List<Implicant> {
	var currentLevel = (minTerms + dontCares).distinct().map { Implicant(it) }
	val allPrimes = mutableSetOf<Implicant>()

	while (currentLevel.isNotEmpty()) {
		val nextLevel = mutableMapOf<Pair<Int, Int>, Implicant>()

		// Optimization: Partition by Mask first, then PopCount
		val maskGroups = currentLevel.groupBy { it.mask }

		for (maskGroup in maskGroups.values) {
			val popCountGroups = maskGroup.groupBy { it.value.countOneBits() }

			for ((count, groupA) in popCountGroups) {
				val groupB = popCountGroups[count + 1] ?: continue

				for (termA in groupA) {
					for (termB in groupB) {
						val combined = termA.tryCombine(termB)
						if (combined != null) {
							termA.isCombined = true
							termB.isCombined = true
							nextLevel[combined.value to combined.mask] = combined
						}
					}
				}
			}
		}

		currentLevel.filter { !it.isCombined }.forEach { allPrimes.add(it) }
		currentLevel = nextLevel.values.toList()
	}

	return allPrimes.toList()
}

internal fun solveSetCover(
	pis: List<Implicant>,
	mintermsToCover: List<MinTerm>
): List<Implicant> {
	val solution = mutableListOf<Implicant>()
	val availablePIs = pis.toMutableList()
	val uncoveredMinterms = mintermsToCover.toMutableList()

	var chartChanged = true

	while (chartChanged && uncoveredMinterms.isNotEmpty()) {
		chartChanged = false

		// 1. Extract Essential Prime Implicants
		val currentMinterms = uncoveredMinterms.toList()
		for (m in currentMinterms) {
			if (!uncoveredMinterms.contains(m)) continue

			val coveringPIs = availablePIs.filter { it.coversMinterm(m) }
			if (coveringPIs.size == 1) {
				val epi = coveringPIs[0]
				solution.add(epi)
				uncoveredMinterms.removeAll { epi.coversMinterm(it) }
				availablePIs.remove(epi)
				chartChanged = true
			}
		}

		// 2. Reduce Chart via Dominance to prevent deep recursion
		if (uncoveredMinterms.isNotEmpty()) {
			if (applyDominance(availablePIs, uncoveredMinterms)) {
				chartChanged = true
			}
		}
	}

	// 3. Branch and Bound for any remaining cyclic core
	if (uncoveredMinterms.isNotEmpty()) {
		val additionalCover = findMinimumCover(availablePIs, uncoveredMinterms)
		if (additionalCover != null) {
			solution.addAll(additionalCover)
		}
	}

	return solution
}

private fun applyDominance(
	availablePIs: MutableList<Implicant>,
	uncoveredMinterms: MutableList<MinTerm>
): Boolean {
	var changed = false

	// Column Dominance (Minterm Dominance)
	val mintermsToRemove = mutableSetOf<MinTerm>()
	for (m1 in uncoveredMinterms) {
		if (mintermsToRemove.contains(m1)) continue
		for (m2 in uncoveredMinterms) {
			if (m1 == m2 || mintermsToRemove.contains(m2)) continue
			val pM1 = availablePIs.filter { it.coversMinterm(m1) }
			val pM2 = availablePIs.filter { it.coversMinterm(m2) }

			if (pM1.isNotEmpty() && pM1.all { pM2.contains(it) }) {
				mintermsToRemove.add(m2)
			}
		}
	}
	if (mintermsToRemove.isNotEmpty()) {
		uncoveredMinterms.removeAll(mintermsToRemove)
		changed = true
	}

	// Row Dominance (PI Dominance)
	val pisToRemove = mutableSetOf<Implicant>()
	for (p1 in availablePIs) {
		if (pisToRemove.contains(p1)) continue
		for (p2 in availablePIs) {
			if (p1 == p2 || pisToRemove.contains(p2)) continue
			val mP1 = uncoveredMinterms.filter { p1.coversMinterm(it) }
			val mP2 = uncoveredMinterms.filter { p2.coversMinterm(it) }

			if (mP2.isNotEmpty() && mP2.all { mP1.contains(it) }) {
				pisToRemove.add(p2)
			}
		}
	}
	if (pisToRemove.isNotEmpty()) {
		availablePIs.removeAll(pisToRemove)
		changed = true
	}

	return changed
}

private fun findMinimumCover(
	availablePIs: List<Implicant>,
	mintermsToCover: List<MinTerm>
): List<Implicant>? {
	if (mintermsToCover.isEmpty()) return emptyList()
	if (availablePIs.isEmpty()) return null

	val targetMinterm = mintermsToCover.first()
	val candidates = availablePIs.filter { it.coversMinterm(targetMinterm) }

	var bestSubSolution: List<Implicant>? = null

	for (candidate in candidates) {
		val newAvailable = availablePIs - candidate
		val newMintermsToCover = mintermsToCover.filter { !candidate.coversMinterm(it) }

		val subSolution = findMinimumCover(newAvailable, newMintermsToCover)?.toMutableList()

		if (subSolution != null) {
			subSolution.add(candidate)
			if (bestSubSolution == null || subSolution.size < bestSubSolution.size) {
				bestSubSolution = subSolution
			}
		}
	}

	return bestSubSolution
}