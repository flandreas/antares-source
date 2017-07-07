package ch.scorpion.antares.model.memory

import ch.scorpion.antares.AntaresTestRule
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [ZeroFiller].
 */
class ZeroFillerTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    @Test
    fun shouldFillLeadingZero() {
        val memory = Memory(8)
        memory.write(1, 11)

        val iter = ZeroFiller(memory.getNonZeroCells())

        assertThat(iter.next(), `is`(MemoryCell(0, 0L)))
        assertThat(iter.next(), `is`(MemoryCell(1, 11L)))
        assertThat(iter.hasNext(), `is`(false))
    }

    @Test
    fun shouldAcceptLeadingValue() {
        val memory = Memory(8)
        memory.write(0, 5)
        memory.write(1, 11)

        val iter = ZeroFiller(memory.getNonZeroCells())

        assertThat(iter.next(), `is`(MemoryCell(0, 5L)))
        assertThat(iter.next(), `is`(MemoryCell(1, 11L)))
        assertThat(iter.hasNext(), `is`(false))
    }

    @Test
    fun shouldFillIntermediateZeros() {
        val memory = Memory(8)
        memory.write(0, 5)
        memory.write(3, 11)

        val iter = ZeroFiller(memory.getNonZeroCells())

        assertThat(iter.next(), `is`(MemoryCell(0, 5L)))
        assertThat(iter.next(), `is`(MemoryCell(1, 0L)))
        assertThat(iter.next(), `is`(MemoryCell(2, 0L)))
        assertThat(iter.next(), `is`(MemoryCell(3, 11L)))
        assertThat(iter.hasNext(), `is`(false))
    }
}