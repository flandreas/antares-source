package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.signal.BitWidth
import kotlin.test.Test
import kotlin.test.assertEquals

class BranchCountTest {

	@Test
	fun shouldYieldForBitWidth() {
		assertEquals(3, BranchCount.forBitWidth(BitWidth.BW_8).size)
	}
}