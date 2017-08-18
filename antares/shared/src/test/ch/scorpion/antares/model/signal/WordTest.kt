package ch.scorpion.antares.model.signal

import ch.scorpion.antares.AntaresModuleJvm
import ch.scorpion.antares.view.AntaresThemes
import ch.scorpion.antares.view.Theme
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test


/**
 * Unit tests for [Word].
 */
class WordTest {

    @Before
    fun setup() {
        DrawModuleJvm.require()
        AntaresThemes.install()
    }

    @Test
    fun shouldBuildFromWords() {
        val list = listOf(Word.of(BitWidth.BW_2, 1L), Word.of(BitWidth.BW_2, 2L))
        assertThat(Word.of(list).getValue(), `is`(9L))
    }

    @Test
    fun shoudBeEqual() {
        assertThat(Word.of(true), `is`(Word.of(true)))
        assertThat(Word.of(BitWidth.BW_4, 7L), `is`(Word.of(BitWidth.BW_4, 7L)))
    }

    @Test
    fun shouldNotBeEqual() {
        assertThat(Word.of(true), `is`(not(Word.of(false))))
        assertThat(Word.of(BitWidth.BW_4, 7L), `is`(not(Word.of(BitWidth.BW_4, 6L))))
        assertThat(Word.of(BitWidth.BW_8, 7L), `is`(not(Word.of(BitWidth.BW_4, 7L))))
    }

    @Test
    fun shouldRetrieveBitAt() {
        assertThat(Word.of(BitWidth.BW_4, 255L).bitAt(0), `is`(Bit.True))
        assertThat(Word.of(BitWidth.BW_4, 255L).bitAt(3), `is`(Bit.True))
        assertThat(Word.of(BitWidth.BW_4, 1L).bitAt(0), `is`(Bit.True))
        assertThat(Word.of(BitWidth.BW_4, 2L).bitAt(0), `is`(Bit.False))
    }

    @Test
    fun shouldExtractSubword() {
        assertThat(Word.of(BitWidth.BW_4, 15L).getSubwordValue(BitWidth.BW_4, 0), `is`(15L))
        assertThat(Word.of(BitWidth.BW_8, 255L).getSubwordValue(BitWidth.BW_4, 0), `is`(15L))
        assertThat(Word.of(BitWidth.BW_8, 255L).getSubwordValue(BitWidth.BW_4, 1), `is`(15L))
        assertThat(Word.of(BitWidth.BW_8, 256L).getSubwordValue(BitWidth.BW_4, 0), `is`(0L))
        assertThat(Word.of(BitWidth.BW_8, 256L).getSubwordValue(BitWidth.BW_4, 1), `is`(0L))
        assertThat(Word.of(BitWidth.BW_8, 7L).getSubwordValue(BitWidth.BW_4, 0), `is`(7L))
        assertThat(Word.of(BitWidth.BW_8, 7L).getSubwordValue(BitWidth.BW_4, 1), `is`(0L))
        assertThat(Word.of(BitWidth.BW_4, 9L).getSubwordValue(BitWidth.BW_2, 0), `is`(1L))
        assertThat(Word.of(BitWidth.BW_4, 9L).getSubwordValue(BitWidth.BW_2, 1), `is`(2L))
    }

    @Test
    fun shouldSetSubword() {
        assertThat(Word.of(BitWidth.BW_4, 15L).withSubwordValue(Word.of(BitWidth.BW_2, 1L), 0), `is`(Word.of(BitWidth.BW_4, 13L)))
        assertThat(Word.of(BitWidth.BW_4, 15L).withSubwordValue(Word.of(BitWidth.BW_2, 1L), 1), `is`(Word.of(BitWidth.BW_4, 7L)))
        assertThat(Word.of(BitWidth.BW_8, 0L).withSubwordValue(Word.of(BitWidth.BW_4, 2L), 1), `is`(Word.of(BitWidth.BW_8, 32L)))
    }

    @Test
    fun shouldSetSubwordWiderThanOrigWord() {
        // Use case: We have a two-bit word of value 0, and we want to enter the value 1 at the left-most position.
        // The value has been entered as a hex number, which is 4 bits wide. We expect the value to be truncated.
        assertThat(Word.of(BitWidth.BW_2, 0L).withSubwordValue(Word.of(BitWidth.BW_4, 1L), 0), `is`(Word.of(BitWidth.BW_2, 1L)))
    }

    @Test
    fun shouldRepresentAsBinary() {
        assertThat(Word.of(BitWidth.BW_4, 6L).toBinaryString(), `is`("0110"))
        assertThat(Word.of(BitWidth.BW_8, 0L).toBinaryString(), `is`("00000000"))
    }

    @Test
    fun shouldRepresentAsHex() {
        assertThat(Word.of(BitWidth.BW_1, 0L).toHexString(), `is`("0"))
        assertThat(Word.of(BitWidth.BW_2, 3L).toHexString(), `is`("3"))
        assertThat(Word.of(BitWidth.BW_4, 15L).toHexString(), `is`("F"))
        assertThat(Word.of(BitWidth.BW_8, 255L).toHexString(), `is`("FF"))
        assertThat(Word.of(BitWidth.BW_8, 15L).toHexString(), `is`("0F"))
        assertThat(Word.of(BitWidth.BW_8, null).toHexString(), `is`("??"))
    }

    @Test
    fun shouldRepresentAsInt() {
        assertThat(Word.of(BitWidth.BW_8, 0L).toInt(), `is`(0))
        assertThat(Word.of(BitWidth.BW_8, 1L).toInt(), `is`(1))
        assertThat(Word.of(BitWidth.BW_8, 37L).toInt(), `is`(37))
        assertThat(Word.of(BitWidth.BW_8, 255L).toInt(), `is`(255))
    }

    @Test
    fun shouldCreateCopyWithChangedBit() {
        assertThat(Word.of(BitWidth.BW_4, 6L).withBit(0, Bit.True), `is`(Word.of(BitWidth.BW_4, 7L)))
    }

    @Test
    fun shouldCheckZeroWithChangedBit() {
        assertThat(Word.of(BitWidth.BW_4, 0L).withBit(0, Bit.True).getColor(), `is`(Theme.current.word))
        assertThat(Word.of(BitWidth.BW_4, 1L).withBit(0, Bit.False).getColor(), `is`(Theme.current.wordZero))
    }

    @Test
    fun shouldReturnBusColor() {
        assertThat(Word.of(BitWidth.BW_4, 1L).getColor(), `is`(Theme.current.word))
    }

    @Test
    fun shouldReturnBusZeroColor() {
        assertThat(Word.of(BitWidth.BW_4, 0L).getColor(), `is`(Theme.current.wordZero))
    }

    @Test
    fun shouldCalculateNot() {
        assertThat(Word.of(true).not() as Word, `is`(Word.of(false)))
        assertThat(Word.of(false).not() as Word, `is`(Word.of(true)))
        assertThat(Word.of(BitWidth.BW_8, 255L).not() as Word, `is`(Word.of(BitWidth.BW_8, 0L)))
    }
}