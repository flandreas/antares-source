package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_1
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_12
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_2
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_4
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_5
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_8
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.Themes
import kotlin.test.*

class WordTest {

	@BeforeTest
	fun setup() {
		DrawModule.require()
		AntaresThemes.install()
		BaseModule.properties.set(DigitalSignalColor.PROP_DIFFERENT_NON_ZERO_MULTI_BIT_COLOR, true)
		DigitalSignalColor.reset()
	}

	@Test
	fun shouldBuildFromWords() {
		val list = listOf(Word.of(BW_2, 1UL), Word.of(BW_2, 2UL))
		assertEquals(9UL, Word.of(list).getValue())
	}

	@Test
	fun shouldBeEqual() {
		assertEquals(Word.of(true), Word.of(true))
		assertEquals(Word.of(BW_4, 7UL), Word.of(BW_4, 7UL))
	}

	@Test
	fun shouldNotBeEqual() {
		assertNotEquals(Word.of(true), Word.of(false))
		assertNotEquals(Word.of(BW_4, 7UL), Word.of(BW_4, 6UL))
		assertNotEquals(Word.of(BW_8, 7UL), Word.of(BW_4, 7UL))
	}

	@Test
	fun shouldBeEqualToDefinedWord() {
		assertTrue(Word.of(BW_4, 3UL).equals(DefinedWord.of(BW_4, 3UL)))
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
		assertEquals('F', Word.of(BW_8, 127UL).nibbleToHexChar(0))
		assertEquals('7', Word.of(BW_8, 127UL).nibbleToHexChar(1))
		assertEquals('X', Word(listOf(True, Error, Undefined, False, True, True, True, True)).nibbleToHexChar(0))
		assertEquals('z', Word(listOf(True, True, True, True, True, True, Undefined, False)).nibbleToHexChar(1))
	}

	@Test
	fun shouldExtractSubword() {
		/** [Word.getSubword] returns [DefinedWord] for performance reasons if all bits are defined. */
		assertEquals(DefinedWord.of(BW_4, 1U), Word.of(BW_1, 1UL).getSubword(BW_4, 0))
		assertEquals(DefinedWord.of(BW_4, 1U), Word.of(BW_4, 1UL).getSubword(BW_4, 0))
		assertEquals(DefinedWord.of(BW_4, 1U), Word.of(BW_8, 16UL).getSubword(BW_4, 1))
	}

	@Test
	fun shouldExtractUnevenSubword() {
		assertEquals(Word.of(BW_4, 1U), Word.of(BW_5, 31UL).getSubword(BW_4, 1))
	}

	@Test
	fun shouldExtractSubwordValue() {
		assertEquals(15UL, Word.of(BW_4, 15UL).getSubwordValue(BW_4, 0))
		assertEquals(15UL, Word.of(BW_8, 255UL).getSubwordValue(BW_4, 0))
		assertEquals(15UL, Word.of(BW_8, 255UL).getSubwordValue(BW_4, 1))
		assertEquals(0UL, Word.of(BW_8, 256UL).getSubwordValue(BW_4, 0))
		assertEquals(0UL, Word.of(BW_8, 256UL).getSubwordValue(BW_4, 1))
		assertEquals(7UL, Word.of(BW_8, 7UL).getSubwordValue(BW_4, 0))
		assertEquals(0UL, Word.of(BW_8, 7UL).getSubwordValue(BW_4, 1))
		assertEquals(1UL, Word.of(BW_4, 9UL).getSubwordValue(BW_2, 0))
		assertEquals(2UL, Word.of(BW_4, 9UL).getSubwordValue(BW_2, 1))
	}

	@Test
	fun shouldExtractUnevenSubwordValue() {
		assertEquals(1UL, Word.of(BW_5, 31UL).getSubwordValue(BW_4, 1))
	}

	@Test
	fun shouldExtractSubwordFromBitWidth2() {
		assertEquals(0UL, Word.of(BW_2, 2U).getSubwordValue(BW_1, 0))
		assertEquals(1UL, Word.of(BW_2, 2U).getSubwordValue(BW_1, 1))
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
		assertEquals(Word.of(BW_4, 13UL), Word.of(BW_4, 15UL).withSubwordValue(Word.of(BW_2, 1UL), 0))
		assertEquals(Word.of(BW_4, 7UL), Word.of(BW_4, 15UL).withSubwordValue(Word.of(BW_2, 1UL), 1))
		assertEquals(Word.of(BW_8, 32UL), Word.of(BW_8, 0UL).withSubwordValue(Word.of(BW_4, 2UL), 1))
	}

	@Test
	fun shouldReplaceAllAsSubword() {
		assertEquals(Word(listOf(False, True, Undefined, False)), Word(listOf(False, True, Undefined, Undefined)).withSubwordValue(Word(listOf(False, True, Undefined, False)), 0))
	}

	@Test
	fun shouldSetSubwordWiderThanOrigWord() {
		// Use case: We have a two-bit word of value 0, and we want to enter the value 1 at the left-most position.
		// The value has been entered as a hex number, which is 4 bits wide. We expect the value to be truncated.
		assertEquals(Word.of(BW_2, 1UL), Word.of(BW_2, 0UL).withSubwordValue(Word.of(BW_4, 1UL), 0))
	}

	@Test
	fun shouldRepresentAsBinary() {
		assertEquals("0001", Word.of(BW_4, 1UL).binaryString)
		assertEquals("0110", Word.of(BW_4, 6UL).binaryString)
		assertEquals("00000000", Word.of(BW_8, 0UL).binaryString)
	}

	@Test
	fun shouldRepresentAsOctal() {
		assertEquals("1", Word.of(BW_4, 1UL).octalString)
		assertEquals("10", Word.of(BW_4, 8UL).octalString)
		assertEquals("12", Word.of(BW_4, 10UL).octalString)
		assertEquals("0", Word.of(BW_8, 0UL).octalString)
	}

	@Test
	fun shouldRepresentAsDecimal() {
		assertEquals("6", Word.of(BW_4, 6UL).decimalString)
		assertEquals("6", Word.of(BW_8, 6UL).decimalString)
	}

	@Test
	fun shouldRepresentAsHex() {
		assertEquals("Z", Word.of(Undefined).hexString)
		assertEquals("z", Word(listOf(False, Undefined)).hexString)
		assertEquals("0", Word.of(BW_1, 0UL).hexString)
		assertEquals("3", Word.of(BW_2, 3UL).hexString)
		assertEquals("F", Word.of(BW_4, 15UL).hexString)
		assertEquals("FF", Word.of(BW_8, 255UL).hexString)
		assertEquals("0F", Word.of(BW_8, 15UL).hexString)
		assertEquals("ZZ", Word.of(BW_8, null).hexString)
	}

	@Test
	fun shouldRepresentAsInt() {
		assertEquals(0, Word.of(BW_8, 0UL).toInt())
		assertEquals(1, Word.of(BW_8, 1UL).toInt())
		assertEquals(37, Word.of(BW_8, 37UL).toInt())
		assertEquals(255, Word.of(BW_8, 255UL).toInt())
	}

	@Test
	fun shouldCreateCopyWithChangedBit() {
		assertEquals(Word.of(BW_4, 7UL), Word.of(BW_4, 6UL).withBit(0, True))
	}

	@Test
	fun shouldCheckZeroWithChangedBit() {
		assertEquals(Themes.get<AntaresTheme>().word, Word.of(BW_4, 0UL).withBit(0, True).color)
		assertEquals(Themes.get<AntaresTheme>().wordZero, Word.of(BW_4, 1UL).withBit(0, False).color)
	}

	@Test
	fun shouldReturnBusColor() {
		assertEquals(Themes.get<AntaresTheme>().word, Word.of(BW_4, 1UL).color)
	}

	@Test
	fun shouldNotReturnBusColorWithoutPreference() {
		BaseModule.properties.set(DigitalSignalColor.PROP_DIFFERENT_NON_ZERO_MULTI_BIT_COLOR, false)
		DigitalSignalColor.reset()
		assertEquals(Themes.get<AntaresTheme>().wordZero, Word.of(BW_4, 1UL).color)
	}

	@Test
	fun shouldReturnBusZeroColor() {
		assertEquals(Themes.get<AntaresTheme>().wordZero, Word.of(BW_4, 0UL).color)
	}

	@Test
	fun shouldCalculateNot() {
		// Same width
		assertEquals(Word.of(false), Word.of(true).not())
		assertEquals(Word.of(true), Word.of(false).not())
		assertEquals(Word.of(BW_8, 0UL), Word.of(BW_8, 255UL).not())
	}

	@Test
	fun shouldCalculateNot0() {
		assertEquals(Word.of(BW_1, 1UL), Word.of(BW_1, 0UL).not())
		assertEquals(1UL, Word.of(BW_1, 0UL).not().toLong())
	}

	@Test
	fun shouldCalculateNot1() {
		assertEquals(Word.of(BW_1, 0UL), Word.of(BW_1, 1UL).not())
		assertEquals(0UL, Word.of(BW_1, 1UL).not().toLong())
	}

	@Test
	fun shouldCalculateOr() {
		// Same width
		assertEquals(Word.of(BW_4, 15UL), Word.of(BW_4, 6UL).or(Word.of(BW_4, 9UL)))
		// Other narrower that this
		assertEquals(Word.of(BW_4, 7UL), Word.of(BW_4, 6UL).or(Word.of(true)))
		// Other wider than this
		assertEquals(Word.of(true), Word.of(true).or(Word.of(BW_4, 6UL)))
	}

	@Test
	fun shouldCalculateAnd() {
		// Same width
		assertEquals(Word.of(BW_4, 4UL), Word.of(BW_4, 6UL).and(Word.of(BW_4, 12UL)))
		// Other narrower that this
		assertEquals(Word.of(BW_4, 1UL), Word.of(BW_4, 3UL).and(Word.of(true)))
		// Other wider than this
		assertEquals(Word.of(false), Word.of(true).and(Word.of(BW_4, 6UL)))
	}

	@Test
	fun shouldFlipBit() {
		assertEquals(Word.of(BW_4, 4U), Word.of(BW_4, 12UL).flip(3))
	}

	@Test
	fun shouldExpandToWidth() {
		val expandedWord = Word.of(BW_4, 1UL).ofWidth(BW_8)
		assertEquals(1UL, expandedWord.getValue())
		assertEquals(BW_8, expandedWord.bitWidth)
	}

	@Test
	fun shouldExpandUndefinedToWidth() {
		assertEquals(Word(listOf(Undefined, False)), Word.undefined(BW_1).ofWidth(BW_2))
	}

	@Test
	fun shouldReduceToWidth() {
		val reducedWord = Word.of(BW_8, 1UL).ofWidth(BW_4)
		assertEquals(1UL, reducedWord.getValue())
		assertEquals(BW_4, reducedWord.bitWidth)
	}

	@Test
	fun shouldShiftLeft() {
		assertEquals(Word.of(BW_8, 16UL), Word.of(BW_8, 8UL).shiftLeft())
		assertEquals(Word.of(BW_8, 32UL), Word.of(BW_8, 8UL).shiftLeft(2))
		assertEquals(Word.of(BW_4, 0UL), Word.of(BW_4, 8UL).shiftLeft())
		assertEquals(Word(listOf(False, Undefined, Undefined, Undefined)), Word.undefined(BW_4).shiftLeft())
	}

	@Test
	fun shouldShiftRight() {
		assertEquals(Word.of(BW_8, 4UL), Word.of(BW_8, 8UL).shiftRight())
		assertEquals(Word.of(BW_8, 2UL), Word.of(BW_8, 8UL).shiftRight(2))
		assertEquals(Word.of(BW_4, 0UL), Word.of(BW_4, 1UL).shiftRight())
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
		assertTrue(Word.of(True).isConsistentWith(Word.of(True)))
	}

	@Test
	fun shouldBeConsistentWithUndefinedBits() {
		assertTrue(Word.of(True).isConsistentWith(Word.of(Undefined)))
		assertTrue(Word.of(Undefined).isConsistentWith(Word.of(True)))
		assertTrue(Word(listOf(Undefined, True)).isConsistentWith(Word(listOf(False, True))))
	}

	@Test
	fun shouldNotBeConsistentWithDifferentDefinedBits() {
		assertFalse(Word.of(True).isConsistentWith(Word.of(False)))
	}

	@Test
	fun shouldNotBeConsistentWithDifferentBitWidth() {
		assertFalse(Word(listOf(True, True)).isConsistentWith(Word(listOf(True))))
	}

	@Test
	fun shouldDefineSubword() {
		assertEquals(Word(listOf(True, False)), Word(listOf(True, Undefined)).defineSubword(Word.of(False), 1))
		assertEquals(Word(listOf(True, True)), Word(listOf(True, True)).defineSubword(Word.of(False), 1))
		assertEquals(Word(listOf(False, True, Undefined, Undefined)), Word(listOf(False, True, Undefined, Undefined)).defineSubword(Word(listOf(False, Undefined, Undefined, Undefined)), 0))
	}
	
	@Test
	fun shouldAddWord() {
		assertEquals(Word.of(BW_8, 31UL), Word.of(BW_8, 17UL).add(14U))
	}

	@Test
	fun shouldAddUInt() {
		assertEquals(Word.of(BW_8, 31UL), Word.of(BW_8, 17UL).add(14U))
	}

	@Test
	fun shouldCalculateModulo() {
		assertEquals(Word.of(BW_4, 1UL), Word.of(BW_4, 3UL).mod(2UL))
	}

	@Test
	fun shouldBeGreaterThanWord() {
		assertTrue(Word.of(BW_4, 7UL).isGreaterThan(6UL))
		assertTrue(Word.of(BW_4, 7UL).isGreaterThan(1UL))
		assertFalse(Word.of(BW_4, 7UL).isGreaterThan(7UL))
	}

	@Test
	fun shouldBeGreaterThanULong() {
		assertTrue(Word.of(BW_4, 7UL).isGreaterThan(6UL))
		assertTrue(Word.of(BW_4, 7UL).isGreaterThan(1UL))
		assertFalse(Word.of(BW_4, 7UL).isGreaterThan(7UL))
	}

	@Test
	fun shouldBeGreaterEqualThanWord() {
		assertTrue(Word.of(BW_4, 7UL).isGreaterEqualThan(6UL))
		assertTrue(Word.of(BW_4, 7UL).isGreaterEqualThan(1UL))
		assertFalse(Word.of(BW_4, 7UL).isGreaterEqualThan(8UL))
	}

	@Test
	fun shouldBeGreaterEqualThanULong() {
		assertTrue(Word.of(BW_4, 7UL).isGreaterEqualThan(6UL))
		assertTrue(Word.of(BW_4, 7UL).isGreaterEqualThan(1UL))
		assertFalse(Word.of(BW_4, 7UL).isGreaterEqualThan(8UL))
	}

	@Test
	fun shouldBeSmallerThanWord() {
		assertFalse(Word.of(BW_4, 7UL).isSmallerThan(7UL))
		assertFalse(Word.of(BW_4, 7UL).isSmallerThan(1UL))
		assertTrue(Word.of(BW_4, 7UL).isSmallerThan(8UL))
	}

	@Test
	fun shouldBeSmallerThanULong() {
		assertFalse(Word.of(BW_4, 7UL).isSmallerThan(7UL))
		assertFalse(Word.of(BW_4, 7UL).isSmallerThan(1UL))
		assertTrue(Word.of(BW_4, 6UL).isSmallerThan(7UL))
	}

	@Test
	fun shouldBeSmallerEqualThanWord() {
		assertTrue(Word.of(BW_4, 7UL).isSmallerEqualThan(7UL))
		assertTrue(Word.of(BW_4, 7UL).isSmallerEqualThan(8UL))
		assertFalse(Word.of(BW_4, 7UL).isSmallerEqualThan(6UL))
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

	@Test
	fun shouldRetrieveBitsAt() {
		assertEquals(1UL, Word.of(BW_8, 15UL).bitsAt(3, 2))
		assertEquals(3UL, Word.of(BW_8, 31UL).bitsAt(3, 2))
	}

	@Test
	fun shouldNotRetrieveErrorBitsAt() {
		assertNull(Word.allOf(BW_8, Error).bitsAt(0, 2))
	}

	@Test
	fun shouldRaiseToThePower() {
		assertEquals(Word.of(BW_8, 27UL), Word.of(BW_8, 3UL).power(3))
	}
}