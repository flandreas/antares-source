package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.signal.BitWidth.*
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
		assertEquals(Word.of(BW_1, 0L), BINARY.signalAt(Word.of(BW_8, 0L), 0) as Word)
		assertEquals(Word.of(BW_1, 0L), BINARY.signalAt(Word.of(BW_8, 2L), 0) as Word)
		assertEquals(Word.of(BW_1, 1L), BINARY.signalAt(Word.of(BW_8, 2L), 1) as Word)
		assertEquals(Word.of(BW_1, 1L), BINARY.signalAt(Word.of(BW_8, 3L), 0) as Word)
		assertEquals(Word.of(BW_1, 1L), BINARY.signalAt(Word.of(BW_8, 3L), 1) as Word)
		assertEquals(Word.of(BW_1, 1L), BINARY.signalAt(Word.of(BW_8, 255L), 7) as Word)
		assertEquals(Word.of(BW_1, 1L), BINARY.signalAt(Word.of(BW_16, 256L), 8) as Word)
		assertEquals(Word.of(BW_1, 0L), BINARY.signalAt(Word.of(BW_16, 256L), 0) as Word)
	}

	@Test
	fun shouldRetrieveHexadecimalSignalAt() {
		assertEquals(Word.of(BW_1, 1L), HEXADECIMAL.signalAt(Word.of(BW_1, 1L), 0) as Word)
		assertEquals(Word.of(BW_4, 0L), HEXADECIMAL.signalAt(Word.of(BW_8, 0L), 0) as Word)
		assertEquals(Word.of(BW_4, 15L), HEXADECIMAL.signalAt(Word.of(BW_8, 15L), 0) as Word)
		assertEquals(Word.of(BW_4, 0L), HEXADECIMAL.signalAt(Word.of(BW_8, 16L), 0) as Word)
		assertEquals(Word.of(BW_4, 1L), HEXADECIMAL.signalAt(Word.of(BW_8, 16L), 1) as Word)
		assertEquals(Word.of(BW_4, 15L), HEXADECIMAL.signalAt(Word.of(BW_8, 255L), 0) as Word)
		assertEquals(Word.of(BW_4, 15L), HEXADECIMAL.signalAt(Word.of(BW_8, 255L), 1) as Word)
	}

	@Test
	fun shouldRetrieveDecimalSignalAt() {
		assertEquals(Word.of(BW_1, 1L), DECIMAL.signalAt(Word.of(BW_1, 1L), 0))
		assertEquals(Word.of(BW_4, 3L), DECIMAL.signalAt(Word.of(BW_8, 123L), 0))
		assertEquals(Word.of(BW_4, 2L), DECIMAL.signalAt(Word.of(BW_8, 123L), 1))
		assertEquals(Word.of(BW_4, 1L), DECIMAL.signalAt(Word.of(BW_8, 123L), 2))
	}

	@Test
	fun shouldRepresentBinarySignal() {
		assertEquals("0", BINARY.represent(Word.of(false)))
		assertEquals("1", BINARY.represent(Word.of(true)))
		assertEquals("10", BINARY.represent(Word.of(BW_2, 2L)))
		assertEquals("1111", BINARY.represent(Word.of(BW_4, 15L)))
	}

	@Test
	fun shouldRepresentHexadecimalSignal() {
		assertEquals("0", HEXADECIMAL.represent(Word.of(BW_4, 0L)))
		assertEquals("1", HEXADECIMAL.represent(Word.of(BW_4, 1L)))
		assertEquals("A", HEXADECIMAL.represent(Word.of(BW_4, 10L)))
		assertEquals("10", HEXADECIMAL.represent(Word.of(BW_8, 16L)))
		assertEquals("FF", HEXADECIMAL.represent(Word.of(BW_8, 255L)))
	}

	@Test
	fun shouldReplaceDecimalSubWord() {
		assertEquals(Word.of(BW_8, 129L), DECIMAL.withDigit(Word.of(BW_8, 123L), Word.of(BW_4, 9L), 0))
		assertEquals(Word.of(BW_8, 193L), DECIMAL.withDigit(Word.of(BW_8, 123L), Word.of(BW_4, 9L), 1))
	}

	@Test
	fun shouldNotReplaceDecimalSubWordOutsideRange() {
		// Value 923 cannot be represented with 8 bit, outside range of 255
		assertEquals(Word.of(BW_8, 123L), DECIMAL.withDigit(Word.of(BW_8, 123L), Word.of(BW_4, 9L), 2))
	}
}