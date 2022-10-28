package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.signal.BitWidth

/** Defines the supported number of branches of a [Splitter] or a [Concentrator]. */
data class BranchCount(val count: Int) {

	companion object {

		val PREDEFINED: List<BranchCount> = let {
			val result = mutableListOf<BranchCount>()
			for (i in 2 .. BitWidth.MAX) {
				result.add(BranchCount(i))
			}
			result
		}

		// Backward compatibility: Some old code (especially unit tests) use the former, restricted set
		val BC_2: BranchCount get() = withCount(2)
		val BC_4: BranchCount get() = withCount(4)
		val BC_8: BranchCount get() = withCount(8)

		/** Defines all possible [BranchCount]w for every [BitWidth].*/
		fun forBitWidth(bitWidth: BitWidth): List<BranchCount> =
			PREDEFINED.filter { it.count <= bitWidth.width && bitWidth.width.mod(it.count) == 0 }

		fun withCount(count: Int): BranchCount =
			PREDEFINED.firstOrNull { it.count == count } ?:
				throw IllegalArgumentException("Unknown BranchCount $count")
	}

	override fun toString(): String = count.toString()
}