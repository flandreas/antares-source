package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProbeTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldNotAllowFixedPointFractionSizeLargerThanBitWidth() {
		val probe = Probe(BitWidth.BW_1)

		assertFailsWith(IllegalArgumentException::class) {
			probe.fixedPointFractionSize = 2
		}
	}

	@Test
	fun shouldResetFixedPointConfigWhenChangingBitWidth() {
		val probe = Probe(BitWidth.BW_4)
		probe.fixedPointFractionSize = 2

		probe.bitWidth = BitWidth.BW_1
		assertEquals(0, probe.fixedPointFractionSize)
	}
}