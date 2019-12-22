package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.*

/**
 * Unit tests for [BitOperation].
 */
class BitOperationTest {

	companion object {
		init {
			BaseModule.require()
		}
	}

	@Test
	fun shouldGetBit() {
		assertFalse(BitOperation.getBitAt(0L, 0))
		assertTrue(BitOperation.getBitAt(1L, 0))
		assertFalse(BitOperation.getBitAt(4L, 0))
		assertFalse(BitOperation.getBitAt(4L, 1))
		assertTrue(BitOperation.getBitAt(4L, 2))
	}

	@Test
	fun shouldSetBit() {
		assertEquals(5L, BitOperation.setBitAt(4L, 0))
		assertEquals(128L, BitOperation.setBitAt(0L, 7))
	}

	@Test
	fun shouldClearBit() {
		assertEquals(0L, BitOperation.clearBitAt(4L, 2))
		assertEquals(3L, BitOperation.clearBitAt(7L, 2))
	}

	@Test
	fun shouldCalculatePower() {
		assertEquals(1, BitOperation.power(0))
		assertEquals(2, BitOperation.power(1))
		assertEquals(4, BitOperation.power(2))
		assertEquals(8, BitOperation.power(3))
		assertEquals(256, BitOperation.power(8))
		assertEquals(65536, BitOperation.power(16))
		assertEquals(4_294_967_296, BitOperation.power(32))
	}

	@Test
	fun shouldConvertHexToLong() {
		assertEquals(0L, BitOperation.hexToLong(""))
		assertEquals(0L, BitOperation.hexToLong("0"))
		assertEquals(5L, BitOperation.hexToLong("5"))
		assertEquals(18L, BitOperation.hexToLong("12"))
		assertEquals(10L, BitOperation.hexToLong("A"))
		assertEquals(255L, BitOperation.hexToLong("FF"))
		assertEquals(65535L, BitOperation.hexToLong("FFFF"))
	}

	@Test
	fun shouldCalculateHexDigit() {
		assertEquals('0', BitOperation.hexDigit(0))
		assertEquals('9', BitOperation.hexDigit(9))
		assertEquals('A', BitOperation.hexDigit(10))
		assertEquals('F', BitOperation.hexDigit(15))
	}

	@Test
	fun shouldConvertLongToHex() {
		assertEquals("0", BitOperation.longToHex(0))
		assertEquals("5", BitOperation.longToHex(5))
		assertEquals("A", BitOperation.longToHex(10))
		assertEquals("FF", BitOperation.longToHex(255))
	}

	@Test
	fun shouldConvertLongToHexPadded() {
		assertEquals("3", BitOperation.longToHexPadded(3, BitWidth.BW_2))
		assertEquals("5", BitOperation.longToHexPadded(5, BitWidth.BW_4))
		assertEquals("05", BitOperation.longToHexPadded(5, BitWidth.BW_8))
	}

	@Test
	fun shouldConvertHexDigitToWord() {
		assertEquals(Word.of(BitWidth.BW_4, 5L), BitOperation.hexDigitToWord(BitWidth.BW_4, '5'))
		assertEquals(Word.of(BitWidth.BW_4, 10L), BitOperation.hexDigitToWord(BitWidth.BW_4, 'A'))
		assertNull(BitOperation.hexDigitToWord(BitWidth.BW_4, 'x'))
	}

	@Test
	fun shouldDetectConvertHexDigitToWordOverflow() {
		assertNull(BitOperation.hexDigitToWord(BitWidth.BW_2, '4'))
	}

	@Test
	fun shouldNormalizeHex() {
		assertEquals("05", BitOperation.normalizeHex("05", BitWidth.BW_8))
		assertEquals("AF", BitOperation.normalizeHex("AF", BitWidth.BW_8))
		assertEquals("AF13", BitOperation.normalizeHex("AF13", BitWidth.BW_16))
	}

	@Test
	fun shouldRejectTooLongHexWhenNormalizing() {
		assertNull(BitOperation.normalizeHex("13", BitWidth.BW_2))
		assertNull(BitOperation.normalizeHex("AB", BitWidth.BW_4))
		assertNull(BitOperation.normalizeHex("ABC", BitWidth.BW_8))
	}

	@Test
	fun shouldPadHexWhenNormalizing() {
		assertEquals("00", BitOperation.normalizeHex("", BitWidth.BW_8))
		assertEquals("0F", BitOperation.normalizeHex("F", BitWidth.BW_8))
		assertEquals("0003", BitOperation.normalizeHex("3", BitWidth.BW_16))
	}

	@Test
	fun shouldRejectTooBigDigits() {
		assertNull(BitOperation.normalizeHex("8", BitWidth.BW_2))
		assertNull(BitOperation.normalizeHex("A", BitWidth.BW_2))
	}

	@Test
	fun shouldRejectInvalidDigit() {
		assertNull(BitOperation.normalizeHex("-", BitWidth.BW_2))
		assertNull(BitOperation.normalizeHex("AX", BitWidth.BW_8))
	}
}