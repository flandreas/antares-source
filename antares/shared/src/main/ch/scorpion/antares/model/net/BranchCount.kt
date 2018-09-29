package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.exception.IllegalArgumentException

/** Defines the supported number of branches of a [Splitter] or a [Concentrator]. */
enum class BranchCount(val count: Int) {
	BC_2(2),
	BC_3(3),
	BC_4(4),
	BC_6(6),
	BC_8(8),
	BC_10(10),
	BC_12(12),
	BC_14(14),
	BC_16(16),
	BC_20(20),
	BC_24(24),
	BC_28(28),
	BC_32(32);

	companion object {

		/** Defines all possible [BranchCount]w for every [BitWidth].*/
		private val BRANCH_COUNTS: Map<BitWidth, List<BranchCount>> = mapOf(
			BitWidth.BW_2 to listOf(BC_2),
			BitWidth.BW_4 to listOf(BC_2, BC_4),
			BitWidth.BW_8 to listOf(BC_2, BC_4, BC_8),
			BitWidth.BW_12 to listOf(BC_3, BC_6, BC_12),
			BitWidth.BW_16 to listOf(BC_2, BC_4, BC_8, BC_16),
			BitWidth.BW_20 to listOf(BC_10, BC_20),
			BitWidth.BW_24 to listOf(BC_2, BC_6, BC_12, BC_24),
			BitWidth.BW_28 to listOf(BC_4, BC_14, BC_28),
			BitWidth.BW_32 to listOf(BC_2, BC_4, BC_8, BC_16, BC_32)
		)

		fun forBitWidth(bitWidth: BitWidth): List<BranchCount> {
			return BRANCH_COUNTS[bitWidth]!!
		}

		fun withCount(count: Int): BranchCount {
			for (branchCount in BranchCount.values()) {
				if (branchCount.count == count) {
					return branchCount
				}
			}
			throw IllegalArgumentException("Unknown BranchCount $count")
		}
	}

	override fun toString(): String {
		return count.toString()
	}
}