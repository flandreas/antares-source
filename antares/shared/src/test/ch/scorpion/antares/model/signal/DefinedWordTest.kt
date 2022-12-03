package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_4
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_8
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefinedWordTest {

	@Test
	fun shouldBuild() {
		DefinedWord.of(BW_4, 3UL)
	}

	@Test
	fun shouldBeEqualToWord() {
		assertTrue(DefinedWord.of(BW_4, 3UL).equals(Word.of(BW_4, 3UL)))
	}

	@Test
	fun shouldNot() {
		assertEquals(DefinedWord(BW_4, 13UL), DefinedWord(BW_4, 2UL).not())
	}

	@Test
	fun shouldAnd() {
		assertEquals(DefinedWord(BW_4, 2UL), DefinedWord(BW_4, 3UL).and(DefinedWord(BW_4, 6UL)))
	}

	@Test
	fun shouldOr() {
		assertEquals(DefinedWord(BW_4, 7UL), DefinedWord(BW_4, 3UL).or(DefinedWord(BW_4, 6UL)))
	}

	@Test
	fun shouldGetBits() {
		val bits = DefinedWord(BW_4, 3UL).bits
		assertEquals(4, bits.size)
		assertEquals(listOf(Bit.of(true), Bit.of(true), Bit.of(false), Bit.of(false)), bits)
	}

	@Test
	fun shouldGetHexString() {
		assertEquals("0A", DefinedWord(BW_8, 10UL).hexString)
	}

	@Test
	fun shouldGetBinaryString() {
		assertEquals("0101", DefinedWord(BW_4, 5UL).binaryString)
	}

	@Test
	fun shouldGetDecimalString() {
		assertEquals("5", DefinedWord(BW_4, 5UL).decimalString)
	}

	@Test
	fun shouldGetBitAt() {
		assertEquals(Bit.of(true), DefinedWord(BW_4, 3UL).bitAt(1))
	}

	@Test
	fun shouldFlip() {
		assertEquals(DefinedWord(BW_4, 1UL), DefinedWord(BW_4, 3UL).flip(1))
		assertEquals(DefinedWord(BW_4, 7UL), DefinedWord(BW_4, 3UL).flip(2))
	}

	@Test
	fun shouldShiftLeft() {
		assertEquals(DefinedWord(BW_4, 6UL), DefinedWord(BW_4, 3UL).shiftLeft(1))
	}

	@Test
	fun shouldShiftRight() {
		assertEquals(DefinedWord(BW_4, 1UL), DefinedWord(BW_4, 3UL).shiftRight(1))
	}

	@Test
	fun shouldAdd() {
		assertEquals(DefinedWord(BW_8, 10UL), DefinedWord(BW_8, 3UL).add(7U))
	}

	@Test
	fun shouldSubtract() {
		assertEquals(DefinedWord(BW_8, 4UL), DefinedWord(BW_8, 7UL).subtract(3U))
	}

	@Test
	fun shouldMultiply() {
		assertEquals(DefinedWord(BW_8, 21UL), DefinedWord(BW_8, 7UL).multiply(3U))
	}

	@Test
	fun shouldDivide() {
		assertEquals(DefinedWord(BW_8, 7UL), DefinedWord(BW_8, 21UL).divide(3U))
		assertEquals(DefinedWord(BW_8, 7UL), DefinedWord(BW_8, 22UL).divide(3U))
		assertEquals(DefinedWord(BW_8, 21UL), DefinedWord(BW_8, 21UL).divide(0U))
	}

	@Test
	fun shouldMod() {
		assertEquals(DefinedWord(BW_8, 2UL), DefinedWord(BW_8, 5UL).mod(3UL))
		assertEquals(DefinedWord(BW_8, 0UL), DefinedWord(BW_8, 5UL).mod(0UL))
	}

	@Test
	fun shouldBeGreaterThan() {
		assertTrue(DefinedWord(BW_8, 5UL).isGreaterThan(4UL))
		assertFalse(DefinedWord(BW_8, 5UL).isGreaterThan(6UL))
	}

	@Test
	fun shouldBeGreaterEqualThan() {
		assertTrue(DefinedWord(BW_8, 5UL).isGreaterEqualThan(5UL))
		assertTrue(DefinedWord(BW_8, 5UL).isGreaterEqualThan(4UL))
		assertFalse(DefinedWord(BW_8, 5UL).isGreaterEqualThan(6UL))
	}

	@Test
	fun shouldBeSmallerThan() {
		assertFalse(DefinedWord(BW_8, 5UL).isSmallerThan(4UL))
		assertTrue(DefinedWord(BW_8, 5UL).isSmallerThan(6UL))
	}

	@Test
	fun shouldBeSmallerEqualThan() {
		assertTrue(DefinedWord(BW_8, 5UL).isSmallerEqualThan(5UL))
		assertTrue(DefinedWord(BW_8, 5UL).isSmallerEqualThan(6UL))
		assertFalse(DefinedWord(BW_8, 5UL).isSmallerEqualThan(4UL))
	}

	@Test
	fun shouldGetSubword() {
		assertEquals(DefinedWord(BW_4, 12UL), DefinedWord(BW_8, 60UL).getSubword(BW_4, 0))
		assertEquals(DefinedWord(BW_4, 3UL), DefinedWord(BW_8, 60UL).getSubword(BW_4, 1))
	}

	@Test
	fun shouldSetBit() {
		assertEquals(DefinedWord(BW_4, 11UL), DefinedWord(BW_4, 3UL).withBit(3, Bit.True))
		assertEquals(DefinedWord(BW_4, 2UL), DefinedWord(BW_4, 3UL).withBit(0, Bit.False))
	}

	@Test
	fun shouldCheckAllOf() {
		assertTrue(DefinedWord(BW_4, 15UL).isAllOf(Bit.True))
		assertFalse(DefinedWord(BW_4, 14UL).isAllOf(Bit.True))
		assertTrue(DefinedWord(BW_4, 0UL).isAllOf(Bit.False))
		assertFalse(DefinedWord(BW_4, 3UL).isAllOf(Bit.Undefined))
	}

	@Test
	fun shouldGetNibbleToHex() {
		assertEquals('F', DefinedWord(BW_8, 15UL).nibbleToHexChar(0))
		assertEquals('E', DefinedWord(BW_8, 239UL).nibbleToHexChar(1))
	}
}