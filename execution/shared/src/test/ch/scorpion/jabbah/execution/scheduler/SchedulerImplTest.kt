package ch.scorpion.jabbah.execution.scheduler

import ch.scorpion.jabbah.base.MILLION
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.ControlledTimer
import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.ExecutionTestRule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.*
import ch.scorpion.jabbah.execution.noise.NoNoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.*

/**
 * Unit tests for [SchedulerImpl].
 */
class SchedulerImplTest {

	@BeforeTest
	fun setup() {
		ExecutionTestRule.configure()
	}

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

	/** ---- Basics tests */

	@Test
	fun shouldActAfterDelay() {
		val actor = createActor()
		scheduler.isActive = true
		scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)

		verify { actor.act(any(), any()) }
	}

	@Test
	fun shouldNotActBeforeDelay() {
		val actor = createActor()
		scheduler.isActive = true
		scheduler.signalHandler.requestActingAfter(actor, 200 * MILLION, createActorData())

		timeService.setTimeMillis(150)

		verify(exactly = 0) { actor.act(any(), any()) }
	}

	@Test
	fun shouldActAllWithSameDelay() {
		val actor1 = createActor()
		val actor2 = createActor()
		scheduler.isActive = true
		scheduler.signalHandler.requestActingAfter(actor1, 100 * MILLION, createActorData())
		scheduler.signalHandler.requestActingAfter(actor2, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)

		verify(exactly = 1) { actor1.act(any(), any()) }
		verify(exactly = 1) { actor2.act(any(), any()) }
	}

	@Test
	fun shouldForgetAfterActing() {
		val actor = createActor()
		scheduler.isActive = true
		scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)
		timeService.setTimeMillis(300)

		verify(exactly = 1) { actor.act(any(), any()) }
		assertEquals(0, scheduler.numberOfRemainingSlots)
	}

	@Test
	fun shouldActOnlyOnceWhenRequestedTwice() {
		val actor = createActor()
		scheduler.isActive = true
		scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())
		scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)

		verify(exactly = 1) { actor.act(any(), any()) }
	}

	/** ---- Stepping tests */

	@Test
	fun shouldNotAllowResumeWhenNotActive() {
		assertFailsWith<IllegalStateException> {
			scheduler.isPaused = true
			scheduler.isActive = false
			scheduler.resume()
		}
	}

	@Test
	fun shouldNotAllowResumeWhenNotPaused() {
		assertFailsWith<IllegalStateException> {
			scheduler.isPaused = false
			scheduler.isActive = true
			scheduler.resume()
		}
	}

	@Test
	fun shouldPause() {
		val actor = createActor()
		scheduler.isActive = true
		scheduler.isPaused = true
		scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)

		verify(exactly = 0) { actor.act(any(), any()) }
	}

	@Test
	fun shouldStep() {
		val actor1 = createActor()
		val actor2 = createActor()
		scheduler.isActive = true
		scheduler.isPaused = true
		scheduler.signalHandler.requestActingAfter(actor1, 100 * MILLION, createActorData())
		scheduler.signalHandler.requestActingAfter(actor2, 200 * MILLION, createActorData())

		timeService.setTimeMillis(150)
		scheduler.resume()
		verify(exactly = 1) { actor1.act(any(), any()) }
		verify(exactly = 0) { actor2.act(any(), any()) }

		timeService.setTimeMillis(250)
		scheduler.resume()
		verify(exactly = 1) { actor1.act(any(), any()) }
		verify(exactly = 1) { actor2.act(any(), any()) }
	}

	@Test
	fun shouldNotWaitForRealTimeWhenStepping() {
		val actor1 = createActor()
		val actor2 = createActor()
		scheduler.isActive = true
		scheduler.isPaused = true
		scheduler.signalHandler.requestActingAfter(actor1, 100 * MILLION, createActorData())
		scheduler.signalHandler.requestActingAfter(actor2, 200 * MILLION, createActorData())

		timeService.setTimeMillis(50)
		scheduler.resume()
		verify(exactly = 1) { actor1.act(any(), any()) }
		verify(exactly = 0) { actor2.act(any(), any()) }

		timeService.setTimeMillis(80)
		scheduler.resume()
		verify(exactly = 1) { actor1.act(any(), any()) }
		verify(exactly = 1) { actor2.act(any(), any()) }
	}

	/** ---- Animation tests */

	@Test
	fun shouldWaitForActingDone() {
		val actor1: Actor = mockk(relaxed = true)
		val actor2 = createActor()

		scheduler.isActive = true
		scheduler.signalHandler.requestActingAfter(actor1, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)
		verify(exactly = 1) { actor1.act(any(), any()) }
		verify(exactly = 0) { actor2.act(any(), any()) }

		timeService.setTimeMillis(300)
		verify(exactly = 1) { actor1.act(any(), any()) }
		verify(exactly = 0) { actor2.act(any(), any()) }
	}

	@Test
	fun shouldProcessActingDone() {
		val actor1 = createActor()
		val actor2 = ForwardingActor(actor1)

		scheduler.isActive = true
		scheduler.signalHandler.requestActingAfter(actor2, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)
		assertTrue(actor2.actingCalled)
		verify(exactly = 0) { actor1.act(any(), any()) }
		assertEquals(1, scheduler.numberOfRemainingSlots)

		scheduler.signalHandler.actingDone(actor2, createActorData())
		assertEquals(1, scheduler.numberOfRemainingSlots)

		timeService.setTimeMillis(250)
		verify(exactly = 1) { actor1.act(any(), any()) }
		assertEquals(0, scheduler.numberOfRemainingSlots)
	}

	@Test
	fun shouldAnimateInParallel() {
		val actor1 = createActor()
		val actor2 = ForwardingActor(actor1)
		val actor3 = ForwardingActor(actor1)

		scheduler.isActive = true
		scheduler.signalHandler.requestActingAfter(actor2, 100 * MILLION, createActorData())
		scheduler.signalHandler.requestActingAfter(actor3, 100 * MILLION, createActorData())
		assertEquals(1, scheduler.numberOfRemainingSlots)

		timeService.setTimeMillis(150)
		verify(exactly = 0) { actor1.act(any(), any()) }
		assertTrue(actor2.actingCalled)
		assertTrue(actor3.actingCalled)

		scheduler.signalHandler.actingDone(actor2, createActorData())
		scheduler.signalHandler.actingDone(actor3, createActorData())
		assertEquals(1, scheduler.numberOfRemainingSlots)

		timeService.setTimeMillis(300)
		verify(exactly = 1) { actor1.act(any(), any()) }
	}

	/** ---- [SchedulerImplTest] */

	private fun createActor(isBreakpoint: Boolean = true): Actor {
		val actor = spyk<ActorImpl>()
		every { actor.isBreakpoint } returns isBreakpoint
		return actor
	}

	private fun createActorData(): ActorData = SimpleActorData()

	/**
	 * An [Actor] that does an animation and requires to be called with
	 * [Actor.actingDone], in which it requests scheduling of a target [Actor]
	 */
	private inner class ForwardingActor(
		private val target: Actor,
		private val targetPropDelay: Long = 100
	) : Actor {

		var actingCalled: Boolean = false

		/** ---- [Actor] interface */

		override val id: Int get() = 0

		override var executionError: ExecutionError? = null

		override var state: ActorState = ActorState.NonExecuting
			private set

		override var propagationDelay: Long = 100L

		override val isBreakpoint: Boolean get() = true

		override fun addActorListener(l: ActorListener) {
			// empty
		}

		override fun removeActorListener(l: ActorListener) {
			// empty
		}

		override fun executionStarted(signalHandler: SignalHandler) {
			state = ActorState.Idle
		}

		override fun act(signalHandler: SignalHandler, data: ActorData) {
			state = ActorState.Acting
			actingCalled = true
		}

		override fun actingVisualized(signalHandler: SignalHandler, l: ActorListener, data: ActorData?) {
			// empty
		}

		override fun actingDone(signalHandler: SignalHandler, data: ActorData?) {
			state = ActorState.Idle
			signalHandler.requestActingAfter(target, targetPropDelay, createActorData())
		}

		override fun executionStopped(signalHandler: SignalHandler) {
			state = ActorState.NonExecuting
		}
	}
}