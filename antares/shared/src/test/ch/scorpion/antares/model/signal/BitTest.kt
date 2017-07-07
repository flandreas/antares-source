package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [Bit].
 */
class BitTest {

    @Test
    fun shouldGetNumericalValue() {
        assertThat(Bit.of(true).numericalValue, `is`(1))
        assertThat(Bit.of(false).numericalValue, `is`(0))
    }

    @Test(expected = KotlinNullPointerException::class)
    fun shouldNotGetUndefinedValue() {
        Bit.Undefined.numericalValue
    }

    @Test
    fun shouldProvideForBoolean() {
        assertThat(Bit.of(true), `is`(Bit.True))
        assertThat(Bit.of(false), `is`(Bit.False))
    }

    @Test
    fun shouldProvideForInteger() {
        assertThat(Bit.of(0), `is`(Bit.False))
        assertThat(Bit.of(1), `is`(Bit.True))
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldRejectInvalidInteger() {
        Bit.of(2)
    }

    @Test
    fun shouldBeEqual() {
        assertThat(Bit.of(0), `is`(equalTo(Bit.of(0))))
        assertThat(Bit.of(1), `is`(equalTo(Bit.of(1))))
    }

    @Test
    fun shouldConvertToBinaryString() {
        assertThat(Bit.Undefined.toBinaryString(), `is`("?"))
        assertThat(Bit.Error.toBinaryString(), `is`("E"))
        assertThat(Bit.of(0).toBinaryString(), `is`("0"))
        assertThat(Bit.of(1).toBinaryString(), `is`("1"))
    }

    @Test
    fun shouldConvertToHexString() {
        assertThat(Bit.Undefined.toHexString(), `is`("?"))
        assertThat(Bit.Error.toHexString(), `is`("E"))
        assertThat(Bit.False.toHexString(), `is`("0"))
        assertThat(Bit.True.toHexString(), `is`("1"))
    }

    @Test
    fun shouldCalculateNot() {
        assertThat(Bit.of(true).not(), `is`(Bit.of(false)))
        assertThat(Bit.of(false).not(), `is`(Bit.of(true)))
        assertThat(Bit.Error.not(), `is`(Bit.Error))
        assertThat(Bit.Undefined.not(), `is`(Bit.Undefined))
    }
}