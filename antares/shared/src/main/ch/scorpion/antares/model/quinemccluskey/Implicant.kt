package ch.scorpion.antares.model.quinemccluskey

typealias MinTerm = Int
typealias Literal = Int

internal data class Implicant(
	val minTerms: Set<MinTerm>,
	val binary: String
) {
	val literals: List<Literal> = binary.mapIndexedNotNull { i, c ->
		when (c) {
			'1' -> i + 1
			'0' -> -(i + 1)
			'-' -> null
			else -> error("Bad character in binary string: '$c'")
		}
	}

	var marked: Boolean = false
		private set

	fun mark() {
		marked = true
	}

	fun combine(other: Implicant): Implicant? {
		if (binary == other.binary || minTerms == other.minTerms) return null

		var result = ""
		var difference = 0

		for ((c1, c2) in binary.zip(other.binary)) {
			if (c1 != c2) {
				result += '-'
				difference++
			} else {
				result += c1
			}

			if (difference > 1) return null
		}

		return Implicant(minTerms + other.minTerms, result)
	}

	override fun toString(): String = "Implicant(${minTerms.sorted()}, '$binary')"

	companion object {
		fun minTerm(minTerm: MinTerm, n: Int): Implicant =
			Implicant(setOf(minTerm), minTerm.toBinary(n))
	}
}