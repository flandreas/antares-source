package ch.scorpion.antares.model.signal

import ch.scorpion.antares.view.AntaresThemes
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.Themes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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
		val list = listOf(Word.of(BitWidth.BW_2, 1L), Word.of(BitWidth.BW_2, 2L))
		assertEquals(9L, Word.of(list).getValue())
	}

	@Test
	fun shoudBeEqual() {
		assertEquals(Word.of(true), Word.of(true))
		assertEquals(Word.of(BitWidth.BW_4, 7L), Word.of(BitWidth.BW_4, 7L))
	}

	@Test
	fun shouldNotBeEqual() {
		assertNotEquals(Word.of(true), Word.of(false))
		assertNotEquals(Word.of(BitWidth.BW_4, 7L), Word.of(BitWidth.BW_4, 6L))
		assertNotEquals(Word.of(BitWidth.BW_8, 7L), Word.of(BitWidth.BW_4, 7L))
	}

	@Test
	fun shouldRetrieveBitAt() {
		assertEquals(Bit.True, Word.of(BitWidth.BW_4, 255L).bitAt(0))
		assertEquals(Bit.True, Word.of(BitWidth.BW_4, 255L).bitAt(3))
		assertEquals(Bit.True, Word.of(BitWidth.BW_4, 1L).bitAt(0))
		assertEquals(Bit.False, Word.of(BitWidth.BW_4, 2L).bitAt(0))
	}

	@Test
	fun shouldExtractSubword() {
		assertEquals(15L, Word.of(BitWidth.BW_4, 15L).getSubwordValue(BitWidth.BW_4, 0))
		assertEquals(15L, Word.of(BitWidth.BW_8, 255L).getSubwordValue(BitWidth.BW_4, 0))
		assertEquals(15L, Word.of(BitWidth.BW_8, 255L).getSubwordValue(BitWidth.BW_4, 1))
		assertEquals(0L, Word.of(BitWidth.BW_8, 256L).getSubwordValue(BitWidth.BW_4, 0))
		assertEquals(0L, Word.of(BitWidth.BW_8, 256L).getSubwordValue(BitWidth.BW_4, 1))
		assertEquals(7L, Word.of(BitWidth.BW_8, 7L).getSubwordValue(BitWidth.BW_4, 0))
		assertEquals(0L, Word.of(BitWidth.BW_8, 7L).getSubwordValue(BitWidth.BW_4, 1))
		assertEquals(1L, Word.of(BitWidth.BW_4, 9L).getSubwordValue(BitWidth.BW_2, 0))
		assertEquals(2L, Word.of(BitWidth.BW_4, 9L).getSubwordValue(BitWidth.BW_2, 1))
	}

	@Test
	fun shouldSetSubword() {
		assertEquals(Word.of(BitWidth.BW_4, 13L), Word.of(BitWidth.BW_4, 15L).withSubwordValue(Word.of(BitWidth.BW_2, 1L), 0))
		assertEquals(Word.of(BitWidth.BW_4, 7L), Word.of(BitWidth.BW_4, 15L).withSubwordValue(Word.of(BitWidth.BW_2, 1L), 1))
		assertEquals(Word.of(BitWidth.BW_8, 32L), Word.of(BitWidth.BW_8, 0L).withSubwordValue(Word.of(BitWidth.BW_4, 2L), 1))
	}

	@Test
	fun shouldSetSubwordWiderThanOrigWord() {
		// Use case: We have a two-bit word of value 0, and we want to enter the value 1 at the left-most position.
		// The value has been entered as a hex number, which is 4 bits wide. We expect the value to be truncated.
		assertEquals(Word.of(BitWidth.BW_2, 1L), Word.of(BitWidth.BW_2, 0L).withSubwordValue(Word.of(BitWidth.BW_4, 1L), 0))
	}

	@Test
	fun shouldRepresentAsBinary() {
		assertEquals("0110", Word.of(BitWidth.BW_4, 6L).toBinaryString())
		assertEquals("00000000", Word.of(BitWidth.BW_8, 0L).toBinaryString())
	}

	@Test
	fun shouldRepresentAsHex() {
		assertEquals("0", Word.of(BitWidth.BW_1, 0L).toHexString())
		assertEquals("3", Word.of(BitWidth.BW_2, 3L).toHexString())
		assertEquals("F", Word.of(BitWidth.BW_4, 15L).toHexString())
		assertEquals("FF", Word.of(BitWidth.BW_8, 255L).toHexString())
		assertEquals("0F", Word.of(BitWidth.BW_8, 15L).toHexString())
		assertEquals("??", Word.of(BitWidth.BW_8, null).toHexString())
	}

	@Test
	fun shouldRepresentAsInt() {
		assertEquals(0, Word.of(BitWidth.BW_8, 0L).toInt())
		assertEquals(1, Word.of(BitWidth.BW_8, 1L).toInt())
		assertEquals(37, Word.of(BitWidth.BW_8, 37L).toInt())
		assertEquals(255, Word.of(BitWidth.BW_8, 255L).toInt())
	}

	@Test
	fun shouldCreateCopyWithChangedBit() {
		assertEquals(Word.of(BitWidth.BW_4, 7L), Word.of(BitWidth.BW_4, 6L).withBit(0, Bit.True))
	}

	@Test
	fun shouldCheckZeroWithChangedBit() {
		assertEquals(Themes.get<AntaresTheme>().word, Word.of(BitWidth.BW_4, 0L).withBit(0, Bit.True).getColor())
		assertEquals(Themes.get<AntaresTheme>().wordZero, Word.of(BitWidth.BW_4, 1L).withBit(0, Bit.False).getColor())
	}

	@Test
	fun shouldReturnBusColor() {
		assertEquals(Themes.get<AntaresTheme>().word, Word.of(BitWidth.BW_4, 1L).getColor())
	}

	@Test
	fun shouldReturnBusZeroColor() {
		assertEquals(Themes.get<AntaresTheme>().wordZero, Word.of(BitWidth.BW_4, 0L).getColor())
	}

	@Test
	fun shouldCalculateNot() {
		assertEquals(Word.of(false), Word.of(true).not() as Word)
		assertEquals(Word.of(true), Word.of(false).not() as Word)
		assertEquals(Word.of(BitWidth.BW_8, 0L), Word.of(BitWidth.BW_8, 255L).not() as Word)
	}

	@Test
	fun shouldFlipBit() {
		assertEquals(Word.of(BitWidth.BW_4, 4), Word.of(BitWidth.BW_4, 12L).flip(3) as Word)
	}
}