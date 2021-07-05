package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.Word
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldCreateWithDefaultBitWidth() {
		val constant = Constant()
		assertEquals(BitWidth.BW_1, constant.bitWidth)
	}

	@Test
	fun shouldCreateWithBitWithFromValue() {
		val constant = Constant(DigitalSignalFactory.of(BitWidth.BW_8, 255L))
		assertEquals(BitWidth.BW_8, constant.bitWidth)
	}

	@Test
	fun shouldDecreaseBitWidth() {
		val constant = Constant(DigitalSignalFactory.of(BitWidth.BW_4, 15L))
		constant.bitWidth = BitWidth.BW_2
		assertEquals(BitWidth.BW_2, constant.bitWidth)
	}
}