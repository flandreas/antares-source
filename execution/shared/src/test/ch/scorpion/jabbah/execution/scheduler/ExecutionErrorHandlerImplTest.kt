package ch.scorpion.jabbah.execution.scheduler

import ch.scorpion.jabbah.base.MILLION
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.ControlledTimer
import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.ExecutionTestRule
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.SimpleActorData
import ch.scorpion.jabbah.execution.noise.NoNoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import io.mockk.*
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
		timeService = ControlledTimeService()
		scheduler = SchedulerImpl(
			timeService,
			eventBus,
			NoiseGeneratorHolder(NoNoiseGenerator(), eventBus),
			task = TimedSchedulerTask(ControlledTimer(timeService))
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
		val normalActor = mockk<Actor>()

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
		val error = spyk<ExecutionError>()
		val force = slot<Boolean>()
		every { error.reevaluated(capture(force), any()) } answers { force.captured || timeService.nowNanos() >= reevaluationTime }
		return error
	}

	private fun createActorWithExecutionError(error: ExecutionError): Actor {
		val actor = mockk<Actor>(relaxed = true)
		every { actor.isBreakpoint } returns true
		every { actor.act(any(), any()) } answers {
			scheduler.deferExecutionError(error)
			scheduler.actingDone(actor, null)
		}
		return actor
	}
}
