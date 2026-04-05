package io.antarescircuit.antares.model.quinemccluskey

typealias MinTerm = Int
typealias Literal = Int

internal data class Implicant(
	val value: Int,
	val mask: Int = 0
) {
	// Excluded from primary constructor so it doesn't affect equals/hashCode in Sets
	var isCombined: Boolean = false

	fun coversMinterm(minTerm: MinTerm): Boolean {
		// Fast O(1) bitwise check
		return (minTerm and mask.inv()) == value
	}

	fun tryCombine(other: Implicant): Implicant? {
		if (this.mask != other.mask) return null

		val diff = this.value xor other.value

		// Check if they differ by exactly ONE bit (lightning-fast power of 2 check)
		if (diff != 0 && (diff and (diff - 1)) == 0) {
			return Implicant(this.value and diff.inv(), this.mask or diff)
		}
		return null
	}

	fun toLiterals(n: Int): List<Literal> {
		val literals = mutableListOf<Literal>()
		for (i in 0 until n) {
			val bitPos = (n - 1) - i
			val bitMask = 1 shl bitPos

			// If the bit is NOT a don't care (not in the mask)
			if ((mask and bitMask) == 0) {
				if ((value and bitMask) != 0) {
					literals.add(i + 1) // '1' yields positive literal
				} else {
					literals.add(-(i + 1)) // '0' yields negative literal
				}
			}
		}
		return literals
	}
}