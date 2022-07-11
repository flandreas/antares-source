package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WiredOrNetSignalApplierTest {

	@Test
	fun shouldEqualSignalBeConsistent() {
		assertTrue(WiredOrNetSignalApplier.signalsAreConsistent(
			DigitalSignalFactory.of(BitWidth.BW_4, 7),
			DigitalSignalFactory.of(BitWidth.BW_4, 7)))
	}

	@Test
	fun shouldDefinedSignalsOfEqualSizeBeConsistent() {
		assertTrue(WiredOrNetSignalApplier.signalsAreConsistent(
			DigitalSignalFactory.of(BitWidth.BW_4, 7),
			DigitalSignalFactory.of(BitWidth.BW_4, 3)))
	}

	@Test
	fun shouldPerformOrWhenApplyingSignals() {
		assertEquals(
			DigitalSignalFactory.of(BitWidth.BW_4, 3),
			WiredOrNetSignalApplier.calculateSignal(
				DigitalSignalFactory.of(BitWidth.BW_4, 1),
				DigitalSignalFactory.of(BitWidth.BW_4, 2)
			))
	}
}