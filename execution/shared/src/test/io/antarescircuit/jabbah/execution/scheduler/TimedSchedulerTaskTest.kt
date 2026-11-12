package io.antarescircuit.jabbah.execution.scheduler

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.base.time.ControlledTimeService
import io.antarescircuit.jabbah.base.time.ControlledTimer
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.base.time.TimeService
import io.antarescircuit.jabbah.execution.ExecutionTestRule
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimedSchedulerTaskTest {

	@Test
	fun shouldYieldAtTimeBudgetAndContinueOnNextTick() {
		var elapsed = 0L
		var calls = 0
		val scheduler = object : Scheduler by mock<Scheduler>(MockMode.autofill) {
			override val isQueueEmpty: Boolean get() = false
			override fun execute(): ExecutionStepResult {
				calls++
				elapsed += 2_000_000
				return ExecutionStepResult.RECALCULATED_NO_BREAKPOINT
			}
		}
		val timeService = object : TimeService {
			override fun nowNanos(): Long = elapsed
			override fun nowMillis(): Long = elapsed / 1_000_000
		}
		val task = createTask(scheduler, timeService)
		task.actionPerformed(event())
		assertEquals(4, calls)
		task.actionPerformed(event())
		assertEquals(8, calls)
		task.dispose()
	}

	@Test
	fun shouldKeepFastCircuitsThroughputAndRespectBreakpoints() {
		var calls = 0
		var breakpoint = false
		val scheduler = object : Scheduler by mock<Scheduler>(MockMode.autofill) {
			override val isQueueEmpty: Boolean get() = false
			override fun execute(): ExecutionStepResult {
				calls++
				return ExecutionStepResult(true, breakpoint)
			}
		}
		val task = createTask(scheduler, ControlledTimeService())
		task.actionPerformed(event())
		assertTrue(calls in TimedSchedulerTask.MAX_SPEED_STEPS..TimedSchedulerTask.MAX_SPEED_STEPS + 2)
		calls = 0
		breakpoint = true
		task.actionPerformed(event())
		assertEquals(1, calls)
		task.dispose()
	}

	private fun createTask(scheduler: Scheduler, timeService: TimeService): TimedSchedulerTask {
		ExecutionTestRule.configure()
		val bus = EventBusImpl()
		val speed = CurrentSystemSpeedCategory(SystemSpeed(speed = SystemSpeed.MAX_SPEED, eventBus = bus), bus)
		return TimedSchedulerTask(speed, ControlledTimer(ControlledTimeService()), bus, timeService).also {
			it.bind(scheduler)
		}
	}

	private fun event(): ActionEvent = ActionEvent(null, this, 0, "tick", 0)
}
