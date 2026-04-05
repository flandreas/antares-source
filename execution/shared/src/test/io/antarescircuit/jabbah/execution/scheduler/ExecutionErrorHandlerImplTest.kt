package io.antarescircuit.jabbah.execution.scheduler

import io.antarescircuit.jabbah.base.math.MILLION
import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.base.time.ControlledTimeService
import io.antarescircuit.jabbah.base.time.ControlledTimer
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.execution.ExecutionError
import io.antarescircuit.jabbah.execution.ExecutionTestRule
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.execution.actor.SimpleActorData
import io.antarescircuit.jabbah.execution.noise.NoNoiseGenerator
import io.antarescircuit.jabbah.execution.noise.NoiseGeneratorHolder
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import dev.mokkery.*
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [ExecutionErrorHandlerImpl] as part of [SchedulerImpl]. */
class ExecutionErrorHandlerImplTest {

	private val timeService: ControlledTimeService
	private val eventBus = EventBusImpl()
	private val scheduler: SchedulerImpl

	init {
		ExecutionTestRule.configure()

		val currentSystemSpeedCategory = CurrentSystemSpeedCategory(SystemSpeed(speed = SystemSpeed.MAX_SPEED, eventBus = eventBus), eventBus)
		timeService = ControlledTimeService()
		scheduler = SchedulerImpl(
			currentSystemSpeedCategory,
			timeService,
			eventBus,
			NoiseGeneratorHolder(NoNoiseGenerator(), eventBus),
			task = TimedSchedulerTask(currentSystemSpeedCategory, ControlledTimer(timeService))
		)
	}

	@BeforeTest
	fun startExecution() {
		scheduler.isActive = true
	}

	@Test
	fun shouldNotRemoveExecutionErrorBeforeReevaluationTime() {
		val error = createError(200 * MILLION)
		val errorActor = createActorWithExecutionError(error)
		val normalActor = mock<Actor>()

		scheduler.requestActingAfter(errorActor, 100 * MILLION, SimpleActorData())
		scheduler.requestActingAfter(normalActor, 150 * MILLION, SimpleActorData())

		timeService.setTimeMillis(100)
		verify { error.reevaluated(any(), any()) }
		assertEquals(1, scheduler.executionErrorCount)
	}

	@Test
	fun shouldRemoveExecutionErrorAfterReevaluationTime() {
		val error = createError(100 * MILLION)
		val errorActor = createActorWithExecutionError(error)

		scheduler.requestActingAfter(errorActor, 100 * MILLION, SimpleActorData())

		timeService.setTimeMillis(100)
		verify { error.reevaluated(any(), any()) }
		assertEquals(0, scheduler.executionErrorCount)
	}

	@Test
	fun shouldRemoveExecutionErrorBeforeReevaluationTimeIfQueueIsEmpty() {
		val error = createError(200 * MILLION)
		val errorActor = createActorWithExecutionError(error)

		scheduler.requestActingAfter(errorActor, 100 * MILLION, SimpleActorData())

		timeService.setTimeMillis(100)
		verify { error.reevaluated(any(), any()) }
		assertEquals(0, scheduler.executionErrorCount)
	}

	private fun createError(reevaluationTime: Long): ExecutionError {
		val error = mock<ExecutionError>()
		val force = Capture.slot<Boolean>()
		every { error.reevaluated(capture(force), any()) } calls { force.get() || timeService.nowNanos() >= reevaluationTime }
		return error
	}

	private fun createActorWithExecutionError(error: ExecutionError): Actor {
		val actor = mock<Actor>(MockMode.autofill)
		every { actor.isBreakpoint } returns true
		every { actor.act(any(), any()) } calls {
			scheduler.deferExecutionError(error)
			scheduler.actingDone(actor, null)
		}
		return actor
	}
}
