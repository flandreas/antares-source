package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.BitWidth.*
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.Themes
import kotlin.test.*

/**
 * Unit tests for [Word].
 */
class WordTest {

	companion object {
		init {
			DrawModule.require()
			AntaresThemes.install()
		}
	}

	@Test
	fun shouldBuildFromWords() {
		val list = listOf(Word.of(BW_2, 1L), Word.of(BW_2, 2L))
		assertEquals(9L, Word.of(list).getValue())
	}

	@Test
	fun shouldBeEqual() {
		assertEquals(Word.of(true), Word.of(true))
		assertEquals(Word.of(BW_4, 7L), Word.of(BW_4, 7L))
	}

	@Test
	fun shouldNotBeEqual() {
		assertNotEquals(Word.of(true), Word.of(false))
		assertNotEquals(Word.of(BW_4, 7L), Word.of(BW_4, 6L))
		assertNotEquals(Word.of(BW_8, 7L), Word.of(BW_4, 7L))
	}

	@Test
	fun shouldRetrieveBitAt() {
		assertEquals(True, Word.of(BW_4, 255L).bitAt(0))
		assertEquals(True, Word.of(BW_4, 255L).bitAt(3))
		assertEquals(True, Word.of(BW_4, 1L).bitAt(0))
		assertEquals(False, Word.of(BW_4, 2L).bitAt(0))
	}

	@Test
	fun shouldRepresentNibbleAsHexChar() {
		assertEquals('F', Word.of(BW_8, 127L).nibbleToHexChar(0))
		assertEquals('7', Word.of(BW_8, 127L).nibbleToHexChar(1))
		assertEquals('X', Word(listOf(True, Error, Undefined, False, True, True, True, True)).nibbleToHexChar(0))
		assertEquals('Z', Word(listOf(True, True, True, True, True, True, Undefined, False)).nibbleToHexChar(1))
	}

	@Test
	fun shouldExtractSubword() {
		assertEquals(15L, Word.of(BW_4, 15L).getSubwordValue(BW_4, 0))
		assertEquals(15L, Word.of(BW_8, 255L).getSubwordValue(BW_4, 0))
		assertEquals(15L, Word.of(BW_8, 255L).getSubwordValue(BW_4, 1))
		assertEquals(0L, Word.of(BW_8, 256L).getSubwordValue(BW_4, 0))
		assertEquals(0L, Word.of(BW_8, 256L).getSubwordValue(BW_4, 1))
		assertEquals(7L, Word.of(BW_8, 7L).getSubwordValue(BW_4, 0))
		assertEquals(0L, Word.of(BW_8, 7L).getSubwordValue(BW_4, 1))
		assertEquals(1L, Word.of(BW_4, 9L).getSubwordValue(BW_2, 0))
		assertEquals(2L, Word.of(BW_4, 9L).getSubwordValue(BW_2, 1))
	}

	@Test
	fun shouldExtractSubwordFromBitWidth2() {
		assertEquals(0, Word.of(BW_2, 2).getSubwordValue(BW_1, 0))
		assertEquals(1, Word.of(BW_2, 2).getSubwordValue(BW_1, 1))
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
		assertEquals(Word.of(BW_4, 13L), Word.of(BW_4, 15L).withSubwordValue(Word.of(BW_2, 1L), 0))
		assertEquals(Word.of(BW_4, 7L), Word.of(BW_4, 15L).withSubwordValue(Word.of(BW_2, 1L), 1))
		assertEquals(Word.of(BW_8, 32L), Word.of(BW_8, 0L).withSubwordValue(Word.of(BW_4, 2L), 1))
	}

	@Test
	fun shouldSetSubwordWiderThanOrigWord() {
		// Use case: We have a two-bit word of value 0, and we want to enter the value 1 at the left-most position.
		// The value has been entered as a hex number, which is 4 bits wide. We expect the value to be truncated.
		assertEquals(Word.of(BW_2, 1L), Word.of(BW_2, 0L).withSubwordValue(Word.of(BW_4, 1L), 0))
	}

	@Test
	fun shouldRepresentAsBinary() {
		assertEquals("0001", Word.of(BW_4, 1L).toBinaryString())
		assertEquals("0110", Word.of(BW_4, 6L).toBinaryString())
		assertEquals("00000000", Word.of(BW_8, 0L).toBinaryString())
	}

	@Test
	fun shouldRepresentAsHex() {
		assertEquals("Z", Word.of(Undefined).toHexString())
		assertEquals("0", Word.of(BW_1, 0L).toHexString())
		assertEquals("3", Word.of(BW_2, 3L).toHexString())
		assertEquals("F", Word.of(BW_4, 15L).toHexString())
		assertEquals("FF", Word.of(BW_8, 255L).toHexString())
		assertEquals("0F", Word.of(BW_8, 15L).toHexString())
		assertEquals("ZZ", Word.of(BW_8, null).toHexString())
	}

	@Test
	fun shouldRepresentAsInt() {
		assertEquals(0, Word.of(BW_8, 0L).toInt())
		assertEquals(1, Word.of(BW_8, 1L).toInt())
		assertEquals(37, Word.of(BW_8, 37L).toInt())
		assertEquals(255, Word.of(BW_8, 255L).toInt())
	}

	@Test
	fun shouldCreateCopyWithChangedBit() {
		assertEquals(Word.of(BW_4, 7L), Word.of(BW_4, 6L).withBit(0, True))
	}

	@Test
	fun shouldCheckZeroWithChangedBit() {
		assertEquals(Themes.get<AntaresTheme>().word, Word.of(BW_4, 0L).withBit(0, True).getColor())
		assertEquals(Themes.get<AntaresTheme>().wordZero, Word.of(BW_4, 1L).withBit(0, False).getColor())
	}

	@Test
	fun shouldReturnBusColor() {
		assertEquals(Themes.get<AntaresTheme>().word, Word.of(BW_4, 1L).getColor())
	}

	@Test
	fun shouldReturnBusZeroColor() {
		assertEquals(Themes.get<AntaresTheme>().wordZero, Word.of(BW_4, 0L).getColor())
	}

	@Test
	fun shouldCalculateNot() {
		assertEquals(Word.of(false), Word.of(true).not() as Word)
		assertEquals(Word.of(true), Word.of(false).not() as Word)
		assertEquals(Word.of(BW_8, 0L), Word.of(BW_8, 255L).not() as Word)
	}

	@Test
	fun shouldFlipBit() {
		assertEquals(Word.of(BW_4, 4), Word.of(BW_4, 12L).flip(3) as Word)
	}

	@Test
	fun shouldExpandToWidth() {
		val expandedWord = Word.of(BW_4, 1L).ofWidth(BW_8)
		assertEquals(1, expandedWord.getValue())
		assertEquals(BW_8, expandedWord.getBitWidth())
	}

	@Test
	fun shouldReduceToWidth() {
		val reducedWord = Word.of(BW_8, 1L).ofWidth(BW_4)
		assertEquals(1, reducedWord.getValue())
		assertEquals(BW_4, reducedWord.getBitWidth())
	}

	@Test
	fun shouldShiftLeft() {
		assertEquals(Word.of(BW_8, 16L), Word.of(BW_8, 8L).shiftLeft())
		assertEquals(Word.of(BW_4, 0L), Word.of(BW_4, 8L).shiftLeft())
	}

	@Test
	fun shouldSiftRight() {
		assertEquals(Word.of(BW_8, 4L), Word.of(BW_8, 8L).shiftRight())
		assertEquals(Word.of(BW_4, 0L), Word.of(BW_4, 1L).shiftRight())
	}

	@Test
	fun shouldReplaceBits() {
		val word = Word(listOf(True, False, Undefined, Error))

		val result = word.replaceBy(False) { it == Undefined }

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
}