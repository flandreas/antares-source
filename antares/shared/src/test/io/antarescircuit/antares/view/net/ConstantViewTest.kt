package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.net.Constant
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.LongValueImpl
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