package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.junit.Assert.*
import org.junit.Test
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before

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
        assertThat(history.size, `is`(1))
    }

    @Test
    fun shouldAddChangingSignal() {
        history.add(false, 100)
        history.add(true, 200)
        history.add(false, 300)
        assertThat(history.size, `is`(3))
    }

    @Test
    fun minDelayShouldBeInfiniteWhenEmpty() {
        assertThat(history.minDelay, `is`(Long.MAX_VALUE))
    }

    @Test
    fun shouldCalculateMinDelay() {
        history.add(true, 0)
        history.add(false, 100)
        history.add(true, 150)
        assertThat(history.minDelay, `is`(50L))
    }

    @Test
    fun shouldTruncate() {
        history.add(true, 0)
        history.add(false, 100)
        history.add(true, 200)
        history.truncate(200)

        assertThat(history.size, `is`(1))
        assertThat(history.lastOrNull()!!.time, `is`(200L))
    }

    @Test
    fun shouldNotRecalcuateMinDelayWhenTruncating() {
        history.add(true, 0)
        history.add(false, 100)
        history.add(true, 150)
        history.truncate(150)
        assertThat(history.minDelay, `is`(50L))
    }
}