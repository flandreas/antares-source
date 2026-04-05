package io.antarescircuit.antares.model.signal

import kotlin.test.Test
import kotlin.test.assertEquals

class BitWidthTest {

	@Test
	fun shouldCalculateMax() {
		assertEquals(BitWidth.BW_8, BitWidth.BW_8.max(BitWidth.BW_4))
	}

	// TODO: Deprecated, remove
	@Test
	fun shouldCalculateSmallest() {
		assertEquals(BitWidth.BW_8, BitWidth.smallest(253UL))
		assertEquals(BitWidth.of(9), BitWidth.smallest(256UL))
	}
}