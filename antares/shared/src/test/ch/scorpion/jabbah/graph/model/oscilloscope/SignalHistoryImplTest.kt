package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Unit tests for [SignalHistory]. */
class SignalHistoryImplTest {

    @Before
    fun setup() {
        BaseModuleJvm.require()
    }

    private val history = SignalHistoryImpl<Boolean>()

    @Test
    fun shouldNotAddSameSignal() {
        history.add(false, 100)
        history.add(false, 200)
        assertEquals(1, history.size)
    }

    @Test
    fun shouldAddChangingSignal() {
        history.add(false, 100)
        history.add(true, 200)
        history.add(false, 300)
        assertEquals(3, history.size)
    }

    @Test
    fun minDelayShouldBeInfiniteWhenEmpty() {
        assertEquals(Long.MAX_VALUE, history.minDelay)
    }

    @Test
    fun shouldCalculateMinDelay() {
        history.add(true, 0)
        history.add(false, 100)
        history.add(true, 150)
        assertEquals(50L, history.minDelay)
    }

    @Test
    fun shouldTruncate() {
        history.add(true, 0)
        history.add(false, 100)
        history.add(true, 200)
        history.truncate(200)

        assertEquals(1, history.size)
        assertEquals(200L, history.lastOrNull()!!.time)
    }

    @Test
    fun shouldNotRecalcuateMinDelayWhenTruncating() {
        history.add(true, 0)
        history.add(false, 100)
        history.add(true, 150)
        history.truncate(150)
        assertEquals(50L, history.minDelay)
    }
}