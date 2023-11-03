package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory.of
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ValueTest {

	@Test
	fun testCompare() {
		assertEquals(Value(of(BitWidth.BW_8, 99)), Value(of(BitWidth.BW_8, 99)))
		assertEquals(Value.X, Value(of(BitWidth.BW_8, 99)))
		assertEquals(Value.Z, Value(of(BitWidth.BW_8, 99), Value.Type.UNDEFINED))

		assertNotEquals(Value(of(BitWidth.BW_8, 99)), Value(of(BitWidth.BW_8, 88)))
	}
}