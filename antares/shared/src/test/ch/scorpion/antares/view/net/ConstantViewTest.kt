package ch.scorpion.antares.view.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.net.Constant
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.LongValueImpl
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantViewTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldDecreaseBitWidth() {
		val cv = ConstantView(model = Constant(LongValueImpl(15)))
		cv.bitWidth = BitWidth.BW_2

		assertEquals(BitWidth.BW_2, cv.bitWidth)
	}
}