package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import kotlin.test.Test
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
}