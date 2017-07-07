package ch.scorpion.antares.model.signal

import org.junit.Assert.*
import org.junit.Before
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation.BINARY
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation.HEXADECIMAL
import ch.scorpion.jabbah.base.module.BaseModuleJvm


/**
 * Unit tests for [DigitalSignalRepresentation].
 */
class DigitalSignalRepresentationTest {

    @Before
    fun setup() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldRetrieveBinarySignalAt() {
        assertThat(BINARY.signalAt(Word.of(BitWidth.BW_8, 0L), 0) as Word, `is`(Word.of(BitWidth.BW_1, 0L)))
        assertThat(BINARY.signalAt(Word.of(BitWidth.BW_8, 2L), 0) as Word, `is`(Word.of(BitWidth.BW_1, 0L)))
        assertThat(BINARY.signalAt(Word.of(BitWidth.BW_8, 2L), 1) as Word, `is`(Word.of(BitWidth.BW_1, 1L)))
        assertThat(BINARY.signalAt(Word.of(BitWidth.BW_8, 3L), 0) as Word, `is`(Word.of(BitWidth.BW_1, 1L)))
        assertThat(BINARY.signalAt(Word.of(BitWidth.BW_8, 3L), 1) as Word, `is`(Word.of(BitWidth.BW_1, 1L)))
        assertThat(BINARY.signalAt(Word.of(BitWidth.BW_8, 255L), 7) as Word, `is`(Word.of(BitWidth.BW_1, 1L)))
        assertThat(BINARY.signalAt(Word.of(BitWidth.BW_16, 256L), 8) as Word, `is`(Word.of(BitWidth.BW_1, 1L)))
        assertThat(BINARY.signalAt(Word.of(BitWidth.BW_16, 256L), 0) as Word, `is`(Word.of(BitWidth.BW_1, 0L)))
    }

    @Test
    fun shouldRetrieveHexadecimalSignalAt() {
        assertThat(HEXADECIMAL.signalAt(Word.of(BitWidth.BW_8, 0L), 0) as Word, `is`(Word.of(BitWidth.BW_4, 0L)))
        assertThat(HEXADECIMAL.signalAt(Word.of(BitWidth.BW_8, 15L), 0) as Word, `is`(Word.of(BitWidth.BW_4, 15L)))
        assertThat(HEXADECIMAL.signalAt(Word.of(BitWidth.BW_8, 16L), 0) as Word, `is`(Word.of(BitWidth.BW_4, 0L)))
        assertThat(HEXADECIMAL.signalAt(Word.of(BitWidth.BW_8, 16L), 1) as Word, `is`(Word.of(BitWidth.BW_4, 1L)))
        assertThat(HEXADECIMAL.signalAt(Word.of(BitWidth.BW_8, 255L), 0) as Word, `is`(Word.of(BitWidth.BW_4, 15L)))
        assertThat(HEXADECIMAL.signalAt(Word.of(BitWidth.BW_8, 255L), 1) as Word, `is`(Word.of(BitWidth.BW_4, 15L)))
    }

    @Test
    fun shouldRepresentBinarySignal() {
        assertThat(BINARY.represent(Word.of(false)), `is`("0"))
        assertThat(BINARY.represent(Word.of(true)), `is`("1"))
        assertThat(BINARY.represent(Word.of(BitWidth.BW_2, 2L)), `is`("10"))
        assertThat(BINARY.represent(Word.of(BitWidth.BW_4, 15L)), `is`("1111"))
    }

    @Test
    fun shouldRepresentHexadecimalSignal() {
        assertThat(HEXADECIMAL.represent(Word.of(BitWidth.BW_4, 0L)), `is`("0"))
        assertThat(HEXADECIMAL.represent(Word.of(BitWidth.BW_4, 1L)), `is`("1"))
        assertThat(HEXADECIMAL.represent(Word.of(BitWidth.BW_4, 10L)), `is`("A"))
        assertThat(HEXADECIMAL.represent(Word.of(BitWidth.BW_8, 16L)), `is`("10"))
        assertThat(HEXADECIMAL.represent(Word.of(BitWidth.BW_8, 255L)), `is`("FF"))
    }
}