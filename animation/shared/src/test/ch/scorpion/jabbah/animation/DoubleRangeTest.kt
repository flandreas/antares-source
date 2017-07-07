package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.exception.NoSuchElementException
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

/**
 * Unit tests for [DoubleRange].
 */
class DoubleRangeOnceTest {

    @Before
    fun init() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldReturnNext() {
        val range = DoubleRange(0.0, 2.0, SequenceType.ONCE)

        assertThat(range.size, `is`(2.0))
        assertThat(range.getNext(1.0), `is`(0.0))
        assertThat(range.getNext(1.0), `is`(1.0))
        assertThat(range.getNext(1.0), `is`(2.0))
        assertThat(range.hasNext(), `is`(false))
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

        assertThat(range.size, `is`(2.0))
        assertThat(range.getNext(1.0), `is`(2.0))
        assertThat(range.getNext(1.0), `is`(1.0))
        assertThat(range.getNext(1.0), `is`(0.0))
        assertThat(range.hasNext(), `is`(false))
    }
}

class DoubleRangeOscillationTests {

    @Before
    fun init() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldOscillate() {
        val range = DoubleRange(0.0, 2.0, SequenceType.OSCILLATION)

        assertThat(range.getNext(1.0), `is`(0.0))
        assertThat(range.getNext(1.0), `is`(1.0))
        assertThat(range.getNext(1.0), `is`(2.0))
        assertThat(range.getNext(1.0), `is`(1.0))
        assertThat(range.getNext(1.0), `is`(0.0))
        assertThat(range.getNext(1.0), `is`(1.0))
    }
}