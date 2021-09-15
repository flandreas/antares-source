package ch.scorpion.antares.model.signal

import kotlin.test.Test
import kotlin.test.assertEquals

class BitWidthTest {

	@Test
	fun shouldCalculateMax() {
		assertEquals(BitWidth.BW_8, BitWidth.BW_8.max(BitWidth.BW_4))
	}
}