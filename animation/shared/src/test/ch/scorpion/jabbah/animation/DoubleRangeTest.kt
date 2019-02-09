package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.exception.NoSuchElementException
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Unit tests for [DoubleRange].
 */
class DoubleRangeOnceTest {

    @BeforeTest
    fun init() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldReturnNext() {
        val range = DoubleRange(0.0, 2.0, SequenceType.ONCE)

        assertEquals(2.0, range.size)
        assertEquals(0.0, range.getNext(1.0))
        assertEquals(1.0, range.getNext(1.0))
        assertEquals(2.0, range.getNext(1.0))
        assertFalse(range.hasNext())
    }

    @Test(expected = NoSuchElementException::class)
    fun shouldEnd() {
        val range = DoubleRange(0.0, 2.0, SequenceType.ONCE)

        range.getNext(1.0)
        range.getNext(1.0)
        range.getNext(1.0)
        range.getNext(1.0)
    }

    @Test
    fun shouldGoBackwards() {
        val range = DoubleRange(2.0, 0.0, SequenceType.ONCE)

        assertEquals(2.0, range.size)
        assertEquals(2.0, range.getNext(1.0))
        assertEquals(1.0, range.getNext(1.0))
        assertEquals(0.0, range.getNext(1.0))
        assertFalse(range.hasNext())
    }
}

class DoubleRangeOscillationTests {

    @BeforeTest
    fun init() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldOscillate() {
        val range = DoubleRange(0.0, 2.0, SequenceType.OSCILLATION)

        assertEquals(0.0, range.getNext(1.0))
        assertEquals(1.0, range.getNext(1.0))
        assertEquals(2.0, range.getNext(1.0))
        assertEquals(1.0, range.getNext(1.0))
        assertEquals(0.0, range.getNext(1.0))
        assertEquals(1.0, range.getNext(1.0))
    }
}