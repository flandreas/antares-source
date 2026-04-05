package io.antarescircuit.antares.view.signal

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class NumberViewTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldCreateBinaryDigits() {
		assertEquals(2, NumberView(BINARY, BitWidth.BW_2).digitCount)
		assertEquals(8, NumberView(BINARY, BitWidth.BW_8).digitCount)
	}

	@Test
	fun shouldCreateHexadecimalDigits() {
		assertEquals(1, NumberView(HEXADECIMAL, BitWidth.BW_2).digitCount)
		assertEquals(2, NumberView(HEXADECIMAL, BitWidth.BW_8).digitCount)
	}

	@Test
	fun shouldCreateDecimalDigits() {
		assertEquals(1, NumberView(DECIMAL, BitWidth.BW_1).digitCount)
		assertEquals(1, NumberView(DECIMAL, BitWidth.BW_2).digitCount)
		assertEquals(2, NumberView(DECIMAL, BitWidth.BW_4).digitCount)
		assertEquals(3, NumberView(DECIMAL, BitWidth.BW_8).digitCount)
	}
}