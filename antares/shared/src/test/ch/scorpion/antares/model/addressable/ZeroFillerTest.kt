package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.AntaresTestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ZeroFillerTest {

    init {
        AntaresTestRule.configure()
    }

    @Test
    fun shouldFillLeadingZero() {
        val memory = Memory(8)
        memory.write(1, 11UL)

        val iter = ZeroFiller(memory.getNonZeroCells())

        assertEquals(MemoryCell(0, 0UL), iter.next())
        assertEquals(MemoryCell(1, 11UL), iter.next())
        assertFalse(iter.hasNext())
    }

    @Test
    fun shouldAcceptLeadingValue() {
        val memory = Memory(8)
        memory.write(0, 5UL)
        memory.write(1, 11UL)

        val iter = ZeroFiller(memory.getNonZeroCells())

        assertEquals(MemoryCell(0, 5UL), iter.next())
        assertEquals(MemoryCell(1, 11UL), iter.next())
        assertFalse(iter.hasNext())
    }

    @Test
    fun shouldFillIntermediateZeros() {
        val memory = Memory(8)
        memory.write(0, 5UL)
        memory.write(3, 11UL)

        val iter = ZeroFiller(memory.getNonZeroCells())

        assertEquals(MemoryCell(0, 5UL), iter.next())
        assertEquals(MemoryCell(1, 0UL), iter.next())
        assertEquals(MemoryCell(2, 0UL), iter.next())
        assertEquals(MemoryCell(3, 11UL), iter.next())
        assertFalse(iter.hasNext())
    }
}