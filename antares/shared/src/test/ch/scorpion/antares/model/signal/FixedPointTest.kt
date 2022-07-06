package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.signal.Bit.Error
import ch.scorpion.antares.model.signal.Bit.True
import ch.scorpion.antares.model.signal.Bit.Undefined
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_16
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_32
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_8
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FixedPointTest {

	@Test
	fun shouldCreateFromDigitalSignal() {
		val fp = FixedPoint(
			FixedPointConfig(3),
			DigitalSignalFactory.of(BW_8, 127))

		assertEquals(15.875, fp.value)
	}

	@Test
	fun shouldCreateWithSign() {
		val fp = FixedPoint(
			FixedPointConfig(3, true),
			DigitalSignalFactory.of(BW_8, 255))

		assertEquals(-15.875, fp.value)
	}

	@Test
	fun shouldCreateFromSpec1() {
		val fp = FixedPoint(
			FixedPointConfig(24),
			DigitalSignalFactory.of(BW_32, 32567536))

		assertEquals(1.941176414489746, fp.value)
	}

	@Test
	fun shouldCreateFromSpec2() {
		val fp = FixedPoint(
			FixedPointConfig(16),
			DigitalSignalFactory.of(BW_32, 32567536))

		assertEquals(496.941162109375, fp.value)
	}

	@Test
	fun shouldBeUndefined() {
		val fp = FixedPoint(
			FixedPointConfig(1),
			DigitalSignalFactory.ofBits(listOf(True, Undefined, True, True)))

		assertEquals(0.0, fp.value)
		assertTrue(fp.isUndefined)
	}

	@Test
	fun shouldBeError() {
		val fp = FixedPoint(
			FixedPointConfig(1),
			DigitalSignalFactory.ofBits(listOf(True, Error, True, True)))

		assertEquals(0.0, fp.value)
		assertTrue(fp.isError)
	}

	@Test
	fun shouldSupportZeroFractionSize() {
		assertEquals(127.0, FixedPoint(
			FixedPointConfig(0),
			DigitalSignalFactory.of(BW_8, 127)).value)
		assertEquals(-127.0, FixedPoint(
			FixedPointConfig(0, true),
			DigitalSignalFactory.of(BW_8, 255)).value)
	}

	@Test
	fun shouldSupportBitWidthFractionSize() {
		assertEquals(0.345672607421875, FixedPoint(
			FixedPointConfig(16),
			DigitalSignalFactory.of(BW_16, 22654)).value)
	}
}