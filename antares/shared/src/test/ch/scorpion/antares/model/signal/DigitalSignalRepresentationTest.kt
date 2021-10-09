package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.signal.BitWidth.*
import ch.scorpion.antares.model.signal.DigitalSignalFactory.allOf
import ch.scorpion.antares.model.signal.DigitalSignalFactory.of
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation.*
import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Unit tests for [DigitalSignalRepresentation].
 */
class DigitalSignalRepresentationTest {

	companion object {
		init {
			BaseModule.require()
		}
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
	fun shouldRetrieveHexadecimalSignalAt() {
		assertEquals(of(BW_1, 1L), HEXADECIMAL.signalAt(of(BW_1, 1L), 0))
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
	fun shouldNotReplaceDecimalUndefinedDigit() {
		assertEquals(of(BW_8, 123L), DECIMAL.withDigit(of(BW_8, 123L), allOf(BW_4, Bit.Undefined), 0))
	}

	@Test
	fun shouldNotReplaceDecimalErrorDigit() {
		assertEquals(of(BW_8, 123L), DECIMAL.withDigit(of(BW_8, 123L), allOf(BW_4, Bit.Error), 0))
	}

	@Test
	fun shouldNotReplaceDecimalSubWordOutsideRange() {
		// Value 923 cannot be represented with 8 bit, outside range of 255
		assertEquals(of(BW_8, 123L), DECIMAL.withDigit(of(BW_8, 123L), of(BW_4, 9L), 2))
	}

	@Test
	fun shouldTrimLeadingZeros() {
		assertEquals("0", BINARY.represent(of(BW_8, 0L)))
		assertEquals("1", BINARY.represent(of(BW_8, 1L)))
		assertEquals("0", HEXADECIMAL.represent(of(BW_8, 0L)))
		assertEquals("1", HEXADECIMAL.represent(of(BW_8, 1L)))
	}
}