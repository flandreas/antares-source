package ch.scorpion.antares.view.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.net.Constant
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.Word
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantViewTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldDecreaseBitWidth() {
		val cv = ConstantView(model = Constant(DigitalSignalFactory.of(BitWidth.BW_4, 15L)))
		cv.bitWidth = BitWidth.BW_2

		assertEquals(BitWidth.BW_2, cv.bitWidth)
	}
}