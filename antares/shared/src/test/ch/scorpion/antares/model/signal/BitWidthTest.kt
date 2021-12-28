package ch.scorpion.antares.model.signal

import kotlin.test.Test
import kotlin.test.assertEquals

class BitWidthTest {

	@Test
	fun shouldCalculateMax() {
		assertEquals(BitWidth.BW_8, BitWidth.BW_8.max(BitWidth.BW_4))
	}

	@Test
	fun shouldCalculateSmallest() {
		assertEquals(BitWidth.BW_8, BitWidth.smallest(253UL))
		assertEquals(BitWidth.BW_12, BitWidth.smallest(256UL))
	}
}