package io.antarescircuit.antares.model.signal

import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_1
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_16
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_2
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_3
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_4
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_5
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_6
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_8
import io.antarescircuit.antares.model.signal.DigitalSignalFactory.of
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation.*
import io.antarescircuit.jabbah.base.module.BaseModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DigitalSignalRepresentationTest {

	init {
		BaseModule.require()
	}

	@Test
	fun shouldRetrieveBinarySignalAt() {
		assertEquals(of(BW_1, 0L), BINARY.signalAt(of(BW_8, 0L), 0))
		assertEquals(of(BW_1, 0L), BINARY.signalAt(of(BW_8, 2L), 0))
		assertEquals(of(BW_1, 1L), BINARY.signalAt(of(BW_8, 2L), 1))
		assertEquals(of(BW_1, 1L), BINARY.signalAt(of(BW_8, 3L), 0))
		assertEquals(of(BW_1, 1L), BINARY.signalAt(of(BW_8, 3L), 1))
		assertEquals(of(BW_1, 1L), BINARY.signalAt(of(BW_8, 255L), 7))
		assertEquals(of(BW_1, 1L), BINARY.signalAt(of(BW_16, 256L), 8))
		assertEquals(of(BW_1, 0L), BINARY.signalAt(of(BW_16, 256L), 0))
	}

	@Test
	fun shouldRetrieveOctalSignalAt() {
		assertEquals(of(BW_3, 0L), OCTAL.signalAt(of(BW_8, 0L), 0))
		assertEquals(of(BW_3, 7L), OCTAL.signalAt(of(BW_8, 15L), 0))
		assertEquals(of(BW_3, 1L), OCTAL.signalAt(of(BW_8, 15L), 1))
	}

	@Test
	fun shouldRetrieveHexadecimalSignalAt() {
		assertEquals(of(BW_4, 1L), HEXADECIMAL.signalAt(of(BW_1, 1L), 0))
		assertEquals(of(BW_4, 0L), HEXADECIMAL.signalAt(of(BW_8, 0L), 0))
		assertEquals(of(BW_4, 15L), HEXADECIMAL.signalAt(of(BW_8, 15L), 0))
		assertEquals(of(BW_4, 0L), HEXADECIMAL.signalAt(of(BW_8, 16L), 0))
		assertEquals(of(BW_4, 1L), HEXADECIMAL.signalAt(of(BW_8, 16L), 1))
		assertEquals(of(BW_4, 15L), HEXADECIMAL.signalAt(of(BW_8, 255L), 0))
		assertEquals(of(BW_4, 15L), HEXADECIMAL.signalAt(of(BW_8, 255L), 1))
	}

	@Test
	fun shouldRetrieveDecimalSignalAt() {
		assertEquals(of(BW_1, 1L), DECIMAL.signalAt(of(BW_1, 1L), 0))
		assertEquals(of(BW_4, 3L), DECIMAL.signalAt(of(BW_8, 123L), 0))
		assertEquals(of(BW_4, 2L), DECIMAL.signalAt(of(BW_8, 123L), 1))
		assertEquals(of(BW_4, 1L), DECIMAL.signalAt(of(BW_8, 123L), 2))
	}

	@Test
	fun shouldRepresentBinarySignal() {
		assertEquals("0", BINARY.represent(of(false)))
		assertEquals("1", BINARY.represent(of(true)))
		assertEquals("10", BINARY.represent(of(BW_2, 2L)))
		assertEquals("1111", BINARY.represent(of(BW_4, 15L)))
	}

	@Test
	fun shouldRepresentHexadecimalSignal() {
		assertEquals("0", HEXADECIMAL.represent(of(BW_4, 0L)))
		assertEquals("1", HEXADECIMAL.represent(of(BW_4, 1L)))
		assertEquals("A", HEXADECIMAL.represent(of(BW_4, 10L)))
		assertEquals("10", HEXADECIMAL.represent(of(BW_8, 16L)))
		assertEquals("FF", HEXADECIMAL.represent(of(BW_8, 255L)))
	}

	@Test
	fun shouldReplaceDecimalSubWord() {
		assertEquals(of(BW_8, 129L), DECIMAL.withDigit(of(BW_8, 123L), of(BW_4, 9L), 0))
		assertEquals(of(BW_8, 193L), DECIMAL.withDigit(of(BW_8, 123L), of(BW_4, 9L), 1))
	}

	@Test
	fun shouldReplaceDecimalUndefinedSubWord() {
		assertEquals(DigitalSignalFactory.undefined(BW_2), DECIMAL.withDigit(of(BW_2, 0), DigitalSignalFactory.undefined(BW_4), 0))
	}

	@Test
	fun shouldNotReplaceDecimalSubWordOutsideRange() {
		// Value 923 cannot be represented with 8 bit, outside range of 255
		assertNull(DECIMAL.withDigit(of(BW_8, 123L), of(BW_4, 9L), 2))
	}

	@Test
	fun shouldReplaceOctalSubWord() {
		assertEquals(of(BW_6, 23L), OCTAL.withDigit(of(BW_6, 15L), of(BW_3, 2L), 1))
	}

	@Test
	fun shouldTrimLeadingZeros() {
		assertEquals("0", BINARY.represent(of(BW_8, 0L)))
		assertEquals("1", BINARY.represent(of(BW_8, 1L)))
		assertEquals("0", HEXADECIMAL.represent(of(BW_8, 0L)))
		assertEquals("1", HEXADECIMAL.represent(of(BW_8, 1L)))
	}

	@Test
	fun shouldCalculateHexDigitCount() {
		assertEquals(1, HEXADECIMAL.digitCount(BW_1))
		assertEquals(1, HEXADECIMAL.digitCount(BW_2))
		assertEquals(1, HEXADECIMAL.digitCount(BW_3))
		assertEquals(1, HEXADECIMAL.digitCount(BW_4))
		assertEquals(2, HEXADECIMAL.digitCount(BW_5))
		assertEquals(2, HEXADECIMAL.digitCount(BW_8))
	}

	/** Regression test for GitHub bug #1143.*/
	@Test
	fun test1143() {
		val signal = DefinedWord(BW_1, 1UL)
		assertEquals("1", DECIMAL.represent(signal))
	}
}