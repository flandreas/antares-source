package io.antarescircuit.antares.model.signal

import io.antarescircuit.jabbah.base.module.BaseModule
import kotlin.test.*

class BitOperationTest {

	init {
		BaseModule.require()
	}

	@Test
	fun shouldGetBit() {
		assertFalse(BitOperation.getBitAt(0UL, 0))
		assertTrue(BitOperation.getBitAt(1UL, 0))
		assertFalse(BitOperation.getBitAt(4UL, 0))
		assertFalse(BitOperation.getBitAt(4UL, 1))
		assertTrue(BitOperation.getBitAt(4UL, 2))
	}

	@Test
	fun shouldSetBit() {
		assertEquals(5UL, BitOperation.setBitAt(4UL, 0))
		assertEquals(128UL, BitOperation.setBitAt(0UL, 7))
	}

	@Test
	fun shouldClearBit() {
		assertEquals(0UL, BitOperation.clearBitAt(4UL, 2))
		assertEquals(3UL, BitOperation.clearBitAt(7UL, 2))
	}

	@Test
	fun shouldCalculatePower() {
		assertEquals(1UL, BitOperation.power(0))
		assertEquals(2UL, BitOperation.power(1))
		assertEquals(4UL, BitOperation.power(2))
		assertEquals(8UL, BitOperation.power(3))
		assertEquals(256UL, BitOperation.power(8))
		assertEquals(65536UL, BitOperation.power(16))
		assertEquals(4_294_967_296UL, BitOperation.power(32))
	}

	@Test
	fun shouldConvertHexToLong() {
		assertEquals(0UL, BitOperation.hexToLong(""))
		assertEquals(0UL, BitOperation.hexToLong("0"))
		assertEquals(5UL, BitOperation.hexToLong("5"))
		assertEquals(18UL, BitOperation.hexToLong("12"))
		assertEquals(10UL, BitOperation.hexToLong("A"))
		assertEquals(10UL, BitOperation.hexToLong("a"))
		assertEquals(255UL, BitOperation.hexToLong("FF"))
		assertEquals(65535UL, BitOperation.hexToLong("FFFF"))
	}

	@Test
	fun shouldConvertBinaryToLong() {
		assertEquals(0UL, BitOperation.binaryToLong(""))
		assertEquals(0UL, BitOperation.binaryToLong("0"))
		assertEquals(1UL, BitOperation.binaryToLong("1"))
		assertEquals(4UL, BitOperation.binaryToLong("0100"))
		assertEquals(15UL, BitOperation.binaryToLong("1111"))
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
		assertEquals("0", BitOperation.longToHex(0UL))
		assertEquals("5", BitOperation.longToHex(5UL))
		assertEquals("A", BitOperation.longToHex(10UL))
		assertEquals("FF", BitOperation.longToHex(255UL))
	}

	@Test
	fun shouldConvertLongToHexPadded() {
		assertEquals("3", BitOperation.longToHexPadded(3UL, BitWidth.BW_2))
		assertEquals("5", BitOperation.longToHexPadded(5UL, BitWidth.BW_4))
		assertEquals("05", BitOperation.longToHexPadded(5UL, BitWidth.BW_8))
	}

	@Test
	fun shouldConvertHexDigitToWord() {
		assertEquals(DigitalSignalFactory.of(BitWidth.BW_4, 5L), BitOperation.hexDigitToWord(BitWidth.BW_4, '5'))
		assertEquals(DigitalSignalFactory.of(BitWidth.BW_4, 10L), BitOperation.hexDigitToWord(BitWidth.BW_4, 'A'))
		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_4, Bit.Undefined), BitOperation.hexDigitToWord(BitWidth.BW_4, 'Z'))
		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_4, Bit.Error), BitOperation.hexDigitToWord(BitWidth.BW_4, 'X'))
	}

	@Test
	fun shouldConvertDecimalDigitToWord() {
		assertEquals(DigitalSignalFactory.of(BitWidth.BW_4, 5L), BitOperation.decimalDigitToWord(BitWidth.BW_4, '5'))
		assertEquals(DigitalSignalFactory.of(BitWidth.BW_4, 0L), BitOperation.decimalDigitToWord(BitWidth.BW_4, '0'))
		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_4, Bit.Undefined), BitOperation.decimalDigitToWord(BitWidth.BW_4, 'Z'))
		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_4, Bit.Error), BitOperation.decimalDigitToWord(BitWidth.BW_4, 'X'))
		assertNull(BitOperation.decimalDigitToWord(BitWidth.BW_4, 'A'))
		assertNull(BitOperation.decimalDigitToWord(BitWidth.BW_2, '4'))
	}

	@Test
	fun shouldConvertBinaryDigitToWord() {
		assertEquals(DigitalSignalFactory.of(false), BitOperation.binaryDigitToWord('0'))
		assertEquals(DigitalSignalFactory.of(true), BitOperation.binaryDigitToWord('1'))
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), BitOperation.binaryDigitToWord('z'))
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), BitOperation.binaryDigitToWord('Z'))
		assertEquals(DigitalSignalFactory.of(Bit.Error), BitOperation.binaryDigitToWord('X'))
		assertEquals(DigitalSignalFactory.of(Bit.Error), BitOperation.binaryDigitToWord('x'))
	}

	@Test
	fun shouldDetectConvertHexDigitToWordOverflow() {
		assertNull(BitOperation.hexDigitToWord(BitWidth.BW_2, '4'))
	}

	@Test
	fun shouldNormalizeHex() {
		assertEquals("1", BitOperation.normalizeHex("1", BitWidth.BW_1))
		assertEquals("2", BitOperation.normalizeHex("2", BitWidth.BW_2))
		assertEquals("3", BitOperation.normalizeHex("3", BitWidth.BW_3))
		assertEquals("05", BitOperation.normalizeHex("05", BitWidth.BW_8))
		assertEquals("AF", BitOperation.normalizeHex("AF", BitWidth.BW_8))
		assertEquals("AF13", BitOperation.normalizeHex("AF13", BitWidth.BW_16))
		assertNull(BitOperation.normalizeHex("", BitWidth.BW_2))
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

	@Test
	fun shouldCalculateBits() {
		assertEquals(1UL, BitOperation.bits(15UL, 3, 2))
		assertEquals(3UL, BitOperation.bits(31UL, 3, 2))
	}
}