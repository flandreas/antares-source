package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.AntaresTestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Unit tests for [ZeroFiller].
 */
class ZeroFillerTest {

    companion object {
	    init {
		    AntaresTestRule.configure()
	    }
    }

    @Test
    fun shouldFillLeadingZero() {
        val memory = Memory(8)
        memory.write(1, 11)

        val iter = ZeroFiller(memory.getNonZeroCells())

        assertEquals(MemoryCell(0, 0L), iter.next())
        assertEquals(MemoryCell(1, 11L), iter.next())
        assertFalse(iter.hasNext())
    }

    @Test
    fun shouldAcceptLeadingValue() {
        val memory = Memory(8)
        memory.write(0, 5)
        memory.write(1, 11)

        val iter = ZeroFiller(memory.getNonZeroCells())

        assertEquals(MemoryCell(0, 5L), iter.next())
        assertEquals(MemoryCell(1, 11L), iter.next())
        assertFalse(iter.hasNext())
    }

    @Test
    fun shouldFillIntermediateZeros() {
        val memory = Memory(8)
        memory.write(0, 5)
        memory.write(3, 11)

        val iter = ZeroFiller(memory.getNonZeroCells())

        assertEquals(MemoryCell(0, 5L), iter.next())
        assertEquals(MemoryCell(1, 0L), iter.next())
        assertEquals(MemoryCell(2, 0L), iter.next())
        assertEquals(MemoryCell(3, 11L), iter.next())
        assertFalse(iter.hasNext())
    }
}