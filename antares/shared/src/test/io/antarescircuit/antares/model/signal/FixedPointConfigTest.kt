package io.antarescircuit.antares.model.signal

import kotlin.test.Test
import kotlin.test.assertEquals

class FixedPointConfigTest {

	@Test
	fun shouldCalculateMinUnsignedValue() {
		val config = FixedPointConfig(16)
		assertEquals(0.0, config.minValue(BitWidth.BW_32))
	}

	@Test
	fun shouldCalculateMaxUnsignedValue() {
		val config = FixedPointConfig(16)
		assertEquals(65535.99998474121, config.maxValue(BitWidth.BW_32))
	}

	@Test
	fun shouldCalculateMinSignedValue() {
		val config = FixedPointConfig(16, signed = true)
		assertEquals(-32768.0, config.minValue(BitWidth.BW_32))
	}

	@Test
	fun shouldCalculateMaxSignedValue() {
		val config = FixedPointConfig(16, signed = true)
		assertEquals(32767.99998474121, config.maxValue(BitWidth.BW_32))
	}
}