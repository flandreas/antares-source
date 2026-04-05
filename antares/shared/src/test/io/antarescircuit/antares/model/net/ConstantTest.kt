package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.LongValueImpl
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldCreateWithDefaultBitWidth() {
		val constant = Constant()
		assertEquals(BitWidth.BW_1, constant.bitWidth)
	}

	@Test
	fun shouldCreateWithBitWithFromValue() {
		val constant = Constant(LongValueImpl(255))
		assertEquals(BitWidth.BW_8, constant.bitWidth)
	}

	@Test
	fun shouldDecreaseBitWidth() {
		val constant = Constant(LongValueImpl(15))
		constant.bitWidth = BitWidth.BW_2
		assertEquals(BitWidth.BW_2, constant.bitWidth)
	}
}