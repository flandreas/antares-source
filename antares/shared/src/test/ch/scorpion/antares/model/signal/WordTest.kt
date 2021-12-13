package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_1
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_12
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_2
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_4
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_8
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.Themes
import kotlin.test.*

class WordTest {

	companion object {
		init {
			DrawModule.require()
			AntaresThemes.install()
		}
	}

	@Test
	fun shouldBuildFromWords() {
		val list = listOf(Word.of(BW_2, 1UL), DigitalSignalFactory.of(BW_2, 2L))
		assertEquals(9UL, Word.of(list).getValue())
	}

	@Test
	fun shouldBeEqual() {
		assertEquals(Word.of(true), DigitalSignalFactory.of(true))
		assertEquals(Word.of(BW_4, 7UL), DigitalSignalFactory.of(BW_4, 7L))
	}

	@Test
	fun shouldNotBeEqual() {
		assertNotEquals(Word.of(true), DigitalSignalFactory.of(false))
		assertNotEquals(Word.of(BW_4, 7UL), DigitalSignalFactory.of(BW_4, 6L))
		assertNotEquals(Word.of(BW_8, 7UL), DigitalSignalFactory.of(BW_4, 7L))
	}

	@Test
	fun shouldRetrieveBitAt() {
		assertEquals(True, Word.of(BW_4, 255UL).bitAt(0))
		assertEquals(True, Word.of(BW_4, 255UL).bitAt(3))
		assertEquals(True, Word.of(BW_4, 1UL).bitAt(0))
		assertEquals(False, Word.of(BW_4, 2UL).bitAt(0))
	}

	@Test
	fun shouldRepresentNibbleAsHexChar() {
		assertEquals('F', DigitalSignalFactory.of(BW_8, 127L).nibbleToHexChar(0))
		assertEquals('7', DigitalSignalFactory.of(BW_8, 127L).nibbleToHexChar(1))
		assertEquals('X', Word(listOf(True, Error, Undefined, False, True, True, True, True)).nibbleToHexChar(0))
		assertEquals('z', Word(listOf(True, True, True, True, True, True, Undefined, False)).nibbleToHexChar(1))
	}

	@Test
	fun shouldExtractSubword() {
		assertEquals(DigitalSignalFactory.of(BW_1, 1), DigitalSignalFactory.of(BW_1, 1L).getSubword(BW_4, 0))
		assertEquals(DigitalSignalFactory.of(BW_4, 1), DigitalSignalFactory.of(BW_4, 1L).getSubword(BW_4, 0))
		assertEquals(DigitalSignalFactory.of(BW_4, 1), DigitalSignalFactory.of(BW_8, 16L).getSubword(BW_4, 1))
	}

	@Test
	fun shouldExtractSubwordValue() {
		assertEquals(15UL, DigitalSignalFactory.of(BW_4, 15L).getSubwordValue(BW_4, 0))
		assertEquals(15UL, DigitalSignalFactory.of(BW_8, 255L).getSubwordValue(BW_4, 0))
		assertEquals(15UL, DigitalSignalFactory.of(BW_8, 255L).getSubwordValue(BW_4, 1))
		assertEquals(0UL, DigitalSignalFactory.of(BW_8, 256L).getSubwordValue(BW_4, 0))
		assertEquals(0UL, DigitalSignalFactory.of(BW_8, 256L).getSubwordValue(BW_4, 1))
		assertEquals(7UL, DigitalSignalFactory.of(BW_8, 7L).getSubwordValue(BW_4, 0))
		assertEquals(0UL, DigitalSignalFactory.of(BW_8, 7L).getSubwordValue(BW_4, 1))
		assertEquals(1UL, DigitalSignalFactory.of(BW_4, 9L).getSubwordValue(BW_2, 0))
		assertEquals(2UL, DigitalSignalFactory.of(BW_4, 9L).getSubwordValue(BW_2, 1))
	}

	@Test
	fun shouldExtractSubwordFromBitWidth2() {
		assertEquals(0UL, DigitalSignalFactory.of(BW_2, 2).getSubwordValue(BW_1, 0))
		assertEquals(1UL, DigitalSignalFactory.of(BW_2, 2).getSubwordValue(BW_1, 1))
	}

	@Test
	fun shouldExtractUndefinedSubwords() {
		assertEquals(
			Word(listOf(True, Undefined)),
			Word(listOf(True, Undefined, Error, False)).getSubword(BW_2, 0)
		)
		assertEquals(
			Word(listOf(Error, False)),
			Word(listOf(True, Undefined, Error, False)).getSubword(BW_2, 1)
		)
	}

	@Test
	fun shouldSetSubword() {
		assertEquals(DigitalSignalFactory.of(BW_4, 13L), DigitalSignalFactory.of(BW_4, 15L).withSubwordValue(DigitalSignalFactory.of(BW_2, 1L), 0))
		assertEquals(DigitalSignalFactory.of(BW_4, 7L), DigitalSignalFactory.of(BW_4, 15L).withSubwordValue(DigitalSignalFactory.of(BW_2, 1L), 1))
		assertEquals(DigitalSignalFactory.of(BW_8, 32L), DigitalSignalFactory.of(BW_8, 0L).withSubwordValue(DigitalSignalFactory.of(BW_4, 2L), 1))
	}

	@Test
	fun shouldReplaceAllAsSubword() {
		assertEquals(Word(listOf(False, True, Undefined, False)), Word(listOf(False, True, Undefined, Undefined)).withSubwordValue(Word(listOf(False, True, Undefined, False)), 0))
	}

	@Test
	fun shouldSetSubwordWiderThanOrigWord() {
		// Use case: We have a two-bit word of value 0, and we want to enter the value 1 at the left-most position.
		// The value has been entered as a hex number, which is 4 bits wide. We expect the value to be truncated.
		assertEquals(DigitalSignalFactory.of(BW_2, 1L), DigitalSignalFactory.of(BW_2, 0L).withSubwordValue(DigitalSignalFactory.of(BW_4, 1L), 0))
	}

	@Test
	fun shouldRepresentAsBinary() {
		assertEquals("0001", DigitalSignalFactory.of(BW_4, 1L).binaryString)
		assertEquals("0110", DigitalSignalFactory.of(BW_4, 6L).binaryString)
		assertEquals("00000000", DigitalSignalFactory.of(BW_8, 0L).binaryString)
	}

	@Test
	fun shouldRepresentAsDecimal() {
		assertEquals("6", DigitalSignalFactory.of(BW_4, 6L).decimalString)
		assertEquals("6", DigitalSignalFactory.of(BW_8, 6L).decimalString)
	}

	@Test
	fun shouldRepresentAsHex() {
		assertEquals("Z", DigitalSignalFactory.of(Undefined).hexString)
		assertEquals("z", Word(listOf(False, Undefined)).hexString)
		assertEquals("0", DigitalSignalFactory.of(BW_1, 0L).hexString)
		assertEquals("3", DigitalSignalFactory.of(BW_2, 3L).hexString)
		assertEquals("F", DigitalSignalFactory.of(BW_4, 15L).hexString)
		assertEquals("FF", DigitalSignalFactory.of(BW_8, 255L).hexString)
		assertEquals("0F", DigitalSignalFactory.of(BW_8, 15L).hexString)
		assertEquals("ZZ", DigitalSignalFactory.of(BW_8, null).hexString)
	}

	@Test
	fun shouldRepresentAsInt() {
		assertEquals(0, DigitalSignalFactory.of(BW_8, 0L).toInt())
		assertEquals(1, DigitalSignalFactory.of(BW_8, 1L).toInt())
		assertEquals(37, DigitalSignalFactory.of(BW_8, 37L).toInt())
		assertEquals(255, DigitalSignalFactory.of(BW_8, 255L).toInt())
	}

	@Test
	fun shouldCreateCopyWithChangedBit() {
		assertEquals(DigitalSignalFactory.of(BW_4, 7L), DigitalSignalFactory.of(BW_4, 6L).withBit(0, True))
	}

	@Test
	fun shouldCheckZeroWithChangedBit() {
		assertEquals(Themes.get<AntaresTheme>().word, DigitalSignalFactory.of(BW_4, 0L).withBit(0, True).color)
		assertEquals(Themes.get<AntaresTheme>().wordZero, DigitalSignalFactory.of(BW_4, 1L).withBit(0, False).color)
	}

	@Test
	fun shouldReturnBusColor() {
		assertEquals(Themes.get<AntaresTheme>().word, DigitalSignalFactory.of(BW_4, 1L).color)
	}

	@Test
	fun shouldReturnBusZeroColor() {
		assertEquals(Themes.get<AntaresTheme>().wordZero, DigitalSignalFactory.of(BW_4, 0L).color)
	}

	@Test
	fun shouldCalculateNot() {
		assertEquals(DigitalSignalFactory.of(false), DigitalSignalFactory.of(true).not())
		assertEquals(DigitalSignalFactory.of(true), DigitalSignalFactory.of(false).not())
		assertEquals(DigitalSignalFactory.of(BW_8, 0L), DigitalSignalFactory.of(BW_8, 255L).not())
	}

	@Test
	fun shouldFlipBit() {
		assertEquals(DigitalSignalFactory.of(BW_4, 4), DigitalSignalFactory.of(BW_4, 12L).flip(3))
	}

	@Test
	fun shouldExpandToWidth() {
		val expandedWord = DigitalSignalFactory.of(BW_4, 1L).ofWidth(BW_8)
		assertEquals(1UL, expandedWord.getValue())
		assertEquals(BW_8, expandedWord.bitWidth)
	}

	@Test
	fun shouldReduceToWidth() {
		val reducedWord = DigitalSignalFactory.of(BW_8, 1L).ofWidth(BW_4)
		assertEquals(1UL, reducedWord.getValue())
		assertEquals(BW_4, reducedWord.bitWidth)
	}

	@Test
	fun shouldShiftLeft() {
		assertEquals(DigitalSignalFactory.of(BW_8, 16L), DigitalSignalFactory.of(BW_8, 8L).shiftLeft())
		assertEquals(DigitalSignalFactory.of(BW_4, 0L), DigitalSignalFactory.of(BW_4, 8L).shiftLeft())
		assertEquals(Word(listOf(False, Undefined, Undefined, Undefined)), Word.undefined(BW_4).shiftLeft())
	}

	@Test
	fun shouldShiftRight() {
		assertEquals(DigitalSignalFactory.of(BW_8, 4L), DigitalSignalFactory.of(BW_8, 8L).shiftRight())
		assertEquals(DigitalSignalFactory.of(BW_4, 0L), DigitalSignalFactory.of(BW_4, 1L).shiftRight())
		assertEquals(Word(listOf(Undefined, Undefined, Undefined, False)), Word.undefined(BW_4).shiftRight())
	}

	@Test
	fun shouldReplaceBits() {
		val word = Word(listOf(True, False, Undefined, Error))

		val result = word.replaceBy(False) { _, bit -> bit == Undefined }

		assertEquals(result, Word(listOf(True, False, False, Error)))
	}

	@Test
	fun shouldBeConsistentWithEqualSignal() {
		assertTrue(DigitalSignalFactory.of(True).isConsistentWith(DigitalSignalFactory.of(True)))
	}

	@Test
	fun shouldBeConsistentWithUndefinedBits() {
		assertTrue(DigitalSignalFactory.of(True).isConsistentWith(DigitalSignalFactory.of(Undefined)))
		assertTrue(DigitalSignalFactory.of(Undefined).isConsistentWith(DigitalSignalFactory.of(True)))
		assertTrue(Word(listOf(Undefined, True)).isConsistentWith(Word(listOf(False, True))))
	}

	@Test
	fun shouldNotBeConsistentWithDifferentDefinedBits() {
		assertFalse(DigitalSignalFactory.of(True).isConsistentWith(DigitalSignalFactory.of(False)))
	}

	@Test
	fun shouldNotBeConsistentWithDifferentBitWidth() {
		assertFalse(Word(listOf(True, True)).isConsistentWith(Word(listOf(True))))
	}

	@Test
	fun shouldDefineSubword() {
		assertEquals(Word(listOf(True, False)), Word(listOf(True, Undefined)).defineSubword(DigitalSignalFactory.of(False), 1))
		assertEquals(Word(listOf(True, True)), Word(listOf(True, True)).defineSubword(DigitalSignalFactory.of(False), 1))
		assertEquals(Word(listOf(False, True, Undefined, Undefined)), Word(listOf(False, True, Undefined, Undefined)).defineSubword(Word(listOf(False, Undefined, Undefined, Undefined)), 0))
	}
	
	@Test
	fun shouldAddWord() {
		assertEquals(Word.of(BW_8, 31UL), Word.of(BW_8, 17UL).add(Word.of(BW_4, 14UL)))
	}

	@Test
	fun shouldAddUUInt() {
		assertEquals(Word.of(BW_8, 31UL), Word.of(BW_8, 17UL).add(14U))
	}

	@Test
	fun shouldCalculateModulo() {
		assertEquals(Word.of(BW_4, 1UL), Word.of(BW_4, 3UL).mod(2UL))
	}

	@Test
	fun shouldBeGreaterThanWord() {
		assertTrue(Word.of(BW_4, 7UL).isGreaterThan(Word.of(BW_4, 6UL)))
		assertTrue(Word.of(BW_4, 7UL).isGreaterThan(Word.of(BW_8, 1UL)))
		assertFalse(Word.of(BW_4, 7UL).isGreaterThan(Word.of(BW_4, 7UL)))
	}

	@Test
	fun shouldBeGreaterThanULong() {
		assertTrue(Word.of(BW_4, 7UL).isGreaterThan(6UL))
		assertTrue(Word.of(BW_4, 7UL).isGreaterThan(1UL))
		assertFalse(Word.of(BW_4, 7UL).isGreaterThan(7UL))
	}

	@Test
	fun shouldBeGreaterEqualThanWord() {
		assertTrue(Word.of(BW_4, 7UL).isGreaterEqualThan(Word.of(BW_4, 6UL)))
		assertTrue(Word.of(BW_4, 7UL).isGreaterEqualThan(Word.of(BW_8, 1UL)))
		assertFalse(Word.of(BW_4, 7UL).isGreaterEqualThan(Word.of(BW_4, 8UL)))
	}

	@Test
	fun shouldBeGreaterEqualThanULong() {
		assertTrue(Word.of(BW_4, 7UL).isGreaterEqualThan(6UL))
		assertTrue(Word.of(BW_4, 7UL).isGreaterEqualThan(1UL))
		assertFalse(Word.of(BW_4, 7UL).isGreaterEqualThan(8UL))
	}

	@Test
	fun shouldBeSmallerThanWord() {
		assertFalse(Word.of(BW_4, 7UL).isSmallerThan(Word.of(BW_4, 7UL)))
		assertFalse(Word.of(BW_4, 7UL).isSmallerThan(Word.of(BW_8, 1UL)))
		assertTrue(Word.of(BW_4, 7UL).isSmallerThan(Word.of(BW_4, 8UL)))
	}

	@Test
	fun shouldBeSmallerThanULong() {
		assertFalse(Word.of(BW_4, 7UL).isSmallerThan(7UL))
		assertFalse(Word.of(BW_4, 7UL).isSmallerThan(1UL))
		assertTrue(Word.of(BW_4, 6UL).isSmallerThan(7UL))
	}

	@Test
	fun shouldBeSmallerEqualThanWord() {
		assertTrue(Word.of(BW_4, 7UL).isSmallerEqualThan(Word.of(BW_4, 7UL)))
		assertTrue(Word.of(BW_4, 7UL).isSmallerEqualThan(Word.of(BW_8, 8UL)))
		assertFalse(Word.of(BW_4, 7UL).isSmallerEqualThan(Word.of(BW_4, 6UL)))
	}

	@Test
	fun shouldBeSmallerEqualThanULong() {
		assertFalse(Word.of(BW_4, 7UL).isSmallerEqualThan(6UL))
		assertFalse(Word.of(BW_4, 7UL).isSmallerEqualThan(1UL))
		assertTrue(Word.of(BW_4, 7UL).isSmallerEqualThan(8UL))
	}

	@Test
	fun shouldReturnWordOfMinimalBitWidth() {
		assertEquals(Word.of(BW_1, 1UL), Word.ofMinimalBitWidth(1UL))
		assertEquals(Word.of(BW_4, 13UL), Word.ofMinimalBitWidth(13UL))
		assertEquals(Word.of(BW_12, 4095UL), Word.ofMinimalBitWidth(4095UL))
	}
}