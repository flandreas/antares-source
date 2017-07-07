package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [BitOperation].
 */
class BitOperationTest {

    @Before
    fun setup() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldGetBit() {
        assertThat(BitOperation.getBitAt(0L, 0), `is`(false))
        assertThat(BitOperation.getBitAt(1L, 0), `is`(true))
        assertThat(BitOperation.getBitAt(4L, 0), `is`(false))
        assertThat(BitOperation.getBitAt(4L, 1), `is`(false))
        assertThat(BitOperation.getBitAt(4L, 2), `is`(true))
    }

    @Test
    fun shouldSetBit() {
        assertThat(BitOperation.setBitAt(4L, 0), `is`(5L))
        assertThat(BitOperation.setBitAt(0L, 7), `is`(128L))
    }

    @Test
    fun shouldClearBit() {
        assertThat(BitOperation.clearBitAt(4L, 2), `is`(0L))
        assertThat(BitOperation.clearBitAt(7L, 2), `is`(3L))
    }

    @Test
    fun shouldCalculatePower() {
        assertThat(BitOperation.power(0), `is`(1))
        assertThat(BitOperation.power(1), `is`(2))
        assertThat(BitOperation.power(2), `is`(4))
        assertThat(BitOperation.power(3), `is`(8))
        assertThat(BitOperation.power(8), `is`(256))
        assertThat(BitOperation.power(16), `is`(65536))
    }

    @Test
    fun shouldConvertHexToLong() {
        assertThat(BitOperation.hexToLong(""), `is`(0L))
        assertThat(BitOperation.hexToLong("0"), `is`(0L))
        assertThat(BitOperation.hexToLong("5"), `is`(5L))
        assertThat(BitOperation.hexToLong("12"), `is`(18L))
        assertThat(BitOperation.hexToLong("A"), `is`(10L))
        assertThat(BitOperation.hexToLong("FF"), `is`(255L))
        assertThat(BitOperation.hexToLong("FFFF"), `is`(65535L))
    }

    @Test
    fun shouldCalculateHexDigit() {
        assertThat(BitOperation.hexDigit(0), `is`('0'))
        assertThat(BitOperation.hexDigit(9), `is`('9'))
        assertThat(BitOperation.hexDigit(10), `is`('A'))
        assertThat(BitOperation.hexDigit(15), `is`('F'))
    }

    @Test
    fun shouldConvertLongToHex() {
        assertThat(BitOperation.longToHex(0), `is`("0"))
        assertThat(BitOperation.longToHex(5), `is`("5"))
        assertThat(BitOperation.longToHex(10), `is`("A"))
        assertThat(BitOperation.longToHex(255), `is`("FF"))
    }

    @Test
    fun shouldConvertHexDigitToWord() {
        assertThat(BitOperation.hexDigitToWord(BitWidth.BW_4, '5'), `is`(Word.of(BitWidth.BW_4, 5L)))
        assertThat(BitOperation.hexDigitToWord(BitWidth.BW_4, 'A'), `is`(Word.of(BitWidth.BW_4, 10L)))
        assertThat(BitOperation.hexDigitToWord(BitWidth.BW_4, 'x'), `is`(nullValue()))
    }

    @Test
    fun shouldDetectConvertHexDigitToWordOverflow() {
        assertThat(BitOperation.hexDigitToWord(BitWidth.BW_2, '4'), `is`(nullValue()))
    }
}