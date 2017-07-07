package ch.scorpion.jabbah.base.time

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test


/**
 * Unit tests for [ControlledTimer].
 */
class ControlledTimerTest {

    private var event: ActionEvent? = null
    lateinit private var timeService: ControlledTimeService

    @Before
    fun setup() {
        BaseModuleJvm.require()
        event = null
        timeService = ControlledTimeService()
    }

    @Test
    fun shouldNotFireBeforeTime() {
        build(100).start()
        timeService.setTimeMillis(50)
        assertThat(event, `is`(nullValue()))
    }

    @Test
    fun shouldFireAtTime() {
        build(100).start()
        timeService.setTimeMillis(100)
        assertThat(event, `is`(notNullValue()))
    }

    @Test
    fun shouldFireAfterTime() {
        build(100).start()
        timeService.setTimeMillis(150)
        assertThat(event, `is`(notNullValue()))
    }

    @Test
    fun shouldNotFireWhenNotStarted() {
        build(100)
        timeService.setTimeMillis(150)
        assertThat(event, `is`(nullValue()))
    }

    @Test
    fun shouldNotFireWhenStopped() {
        val timer = build(100)

        timer.start()
        timeService.setTimeMillis(150)
        assertThat(event, `is`(notNullValue()))

        event = null
        timer.stop()
        timeService.setTimeMillis(250)
        assertThat(event, `is`(nullValue()))
    }

    @Test
    fun shouldFireRepeatedly() {
        build(100).start()
        timeService.setTimeMillis(150)
        assertThat(event, `is`(notNullValue()))
        event = null
        timeService.setTimeMillis(250)
        assertThat(event, `is`(notNullValue()))
    }

    @Test(expected = IllegalStateException::class)
    fun shouldNotStartUninitialized() {
        val timer = ControlledTimer(timeService)
        timer.start()
    }

    private fun build(interval: Int): Timer {
        val timer = ControlledTimer(timeService)
        timer.initialize(interval, {this@ControlledTimerTest.event = it})
        return timer
    }
}