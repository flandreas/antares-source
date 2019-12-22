package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.signal.DigitalSignalRepresentation.BINARY
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation.HEXADECIMAL
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
		assertEquals(Word.of(BitWidth.BW_1, 0L), BINARY.signalAt(Word.of(BitWidth.BW_8, 0L), 0) as Word)
		assertEquals(Word.of(BitWidth.BW_1, 0L), BINARY.signalAt(Word.of(BitWidth.BW_8, 2L), 0) as Word)
		assertEquals(Word.of(BitWidth.BW_1, 1L), BINARY.signalAt(Word.of(BitWidth.BW_8, 2L), 1) as Word)
		assertEquals(Word.of(BitWidth.BW_1, 1L), BINARY.signalAt(Word.of(BitWidth.BW_8, 3L), 0) as Word)
		assertEquals(Word.of(BitWidth.BW_1, 1L), BINARY.signalAt(Word.of(BitWidth.BW_8, 3L), 1) as Word)
		assertEquals(Word.of(BitWidth.BW_1, 1L), BINARY.signalAt(Word.of(BitWidth.BW_8, 255L), 7) as Word)
		assertEquals(Word.of(BitWidth.BW_1, 1L), BINARY.signalAt(Word.of(BitWidth.BW_16, 256L), 8) as Word)
		assertEquals(Word.of(BitWidth.BW_1, 0L), BINARY.signalAt(Word.of(BitWidth.BW_16, 256L), 0) as Word)
	}

	@Test
	fun shouldRetrieveHexadecimalSignalAt() {
		assertEquals(Word.of(BitWidth.BW_4, 0L), HEXADECIMAL.signalAt(Word.of(BitWidth.BW_8, 0L), 0) as Word)
		assertEquals(Word.of(BitWidth.BW_4, 15L), HEXADECIMAL.signalAt(Word.of(BitWidth.BW_8, 15L), 0) as Word)
		assertEquals(Word.of(BitWidth.BW_4, 0L), HEXADECIMAL.signalAt(Word.of(BitWidth.BW_8, 16L), 0) as Word)
		assertEquals(Word.of(BitWidth.BW_4, 1L), HEXADECIMAL.signalAt(Word.of(BitWidth.BW_8, 16L), 1) as Word)
		assertEquals(Word.of(BitWidth.BW_4, 15L), HEXADECIMAL.signalAt(Word.of(BitWidth.BW_8, 255L), 0) as Word)
		assertEquals(Word.of(BitWidth.BW_4, 15L), HEXADECIMAL.signalAt(Word.of(BitWidth.BW_8, 255L), 1) as Word)
	}

	@Test
	fun shouldRepresentBinarySignal() {
		assertEquals("0", BINARY.represent(Word.of(false)))
		assertEquals("1", BINARY.represent(Word.of(true)))
		assertEquals("10", BINARY.represent(Word.of(BitWidth.BW_2, 2L)))
		assertEquals("1111", BINARY.represent(Word.of(BitWidth.BW_4, 15L)))
	}

	@Test
	fun shouldRepresentHexadecimalSignal() {
		assertEquals("0", HEXADECIMAL.represent(Word.of(BitWidth.BW_4, 0L)))
		assertEquals("1", HEXADECIMAL.represent(Word.of(BitWidth.BW_4, 1L)))
		assertEquals("A", HEXADECIMAL.represent(Word.of(BitWidth.BW_4, 10L)))
		assertEquals("10", HEXADECIMAL.represent(Word.of(BitWidth.BW_8, 16L)))
		assertEquals("FF", HEXADECIMAL.represent(Word.of(BitWidth.BW_8, 255L)))
	}
}