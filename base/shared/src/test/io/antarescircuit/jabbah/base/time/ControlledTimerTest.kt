package io.antarescircuit.jabbah.base.time

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import kotlin.test.*

/**
 * Unit tests for [ControlledTimer].
 */
class ControlledTimerTest {

	private var event: ActionEvent? = null
	private lateinit var timeService: ControlledTimeService

	@BeforeTest
	fun setup() {
		BaseModule.require()
		event = null
		timeService = ControlledTimeService()
	}

	@Test
	fun shouldNotFireBeforeTime() {
		build(100).start()
		timeService.setTimeMillis(50)
		assertNull(event)
	}

	@Test
	fun shouldFireAtTime() {
		build(100).start()
		timeService.setTimeMillis(100)
		assertNotNull(event)
	}

	@Test
	fun shouldFireAfterTime() {
		build(100).start()
		timeService.setTimeMillis(150)
		assertNotNull(event)
	}

	@Test
	fun shouldNotFireWhenNotStarted() {
		build(100)
		timeService.setTimeMillis(150)
		assertNull(event)
	}

	@Test
	fun shouldNotFireWhenStopped() {
		val timer = build(100)

		timer.start()
		timeService.setTimeMillis(150)
		assertNotNull(event)

		event = null
		timer.stop()
		timeService.setTimeMillis(250)
		assertNull(event)
	}

	@Test
	fun shouldFireRepeatedly() {
		build(100).start()
		timeService.setTimeMillis(150)
		assertNotNull(event)
		event = null
		timeService.setTimeMillis(250)
		assertNotNull(event)
	}

	@Test
	fun shouldNotStartUninitialized() {
		assertFailsWith<IllegalStateException> {
			val timer = ControlledTimer(timeService)
			timer.start()
		}
	}

	private fun build(interval: Int): Timer {
		val timer = ControlledTimer(timeService)
		timer.initialize(interval) { this@ControlledTimerTest.event = it }
		return timer
	}
}