package ch.scorpion.jabbah.execution.scheduler

import ch.scorpion.jabbah.base.math.MILLION
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.ControlledTimer
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.ExecutionTestRule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.*
import ch.scorpion.jabbah.execution.noise.NoNoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.*

class SchedulerImplTest {

	private val timeService: ControlledTimeService
	private val eventBus = EventBusImpl()
	private val scheduler: SchedulerImpl
	private var breakpointEvent: BreakpointEvent? = null

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
		eventBus.register(BreakpointEvent::class) { breakpointEvent = it }
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
	fun shouldWaitInBreakpoint() {
		val actor = createActor()
		scheduler.isSoftBreakpointsEnabled = true
		scheduler.isSingleStepMode = true
		scheduler.isActive = true
		scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)

		verify(exactly = 0) { actor.act(any(), any()) }
		assertTrue(scheduler.isInBreakpoint)
		assertNotNull(breakpointEvent)
	}

	@Test
	fun shouldWaitInBreakpointWhenPausedDuringExecution() {
		val actor = createActor()
		scheduler.isSoftBreakpointsEnabled = true
		scheduler.isSingleStepMode = false
		scheduler.isActive = true

		scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())
		timeService.setTimeMillis(150)
		verify(exactly = 1) { actor.act(any(), any()) }
		assertNull(breakpointEvent)

		scheduler.isSingleStepMode = true
		scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())

		timeService.setTimeMillis(250)

		verify(exactly = 1) { actor.act(any(), any()) }
		assertTrue(scheduler.isInBreakpoint)
		assertNotNull(breakpointEvent)
	}

	@Test
	fun shouldResumeFromBreakpoint() {
		val actor1 = createActor()
		val actor2 = createActor()
		scheduler.isSoftBreakpointsEnabled = true
		scheduler.isActive = true
		scheduler.isSingleStepMode = true
		scheduler.signalHandler.requestActingAfter(actor1, 100 * MILLION, createActorData())
		scheduler.signalHandler.requestActingAfter(actor2, 200 * MILLION, createActorData())

		timeService.setTimeMillis(150)
		scheduler.systemSpeedCategory.systemSpeed.resume()
		verify(exactly = 1) { actor1.act(any(), any()) }
		verify(exactly = 0) { actor2.act(any(), any()) }

		timeService.setTimeMillis(250)
		scheduler.systemSpeedCategory.systemSpeed.resume()
		verify(exactly = 1) { actor1.act(any(), any()) }
		verify(exactly = 1) { actor2.act(any(), any()) }
	}

	@Test
	fun shouldNotWaitForRealTimeWhenResuming() {
		val actor1 = createActor()
		val actor2 = createActor()
		scheduler.isSoftBreakpointsEnabled = true
		scheduler.isActive = true
		scheduler.isSingleStepMode = true
		scheduler.signalHandler.requestActingAfter(actor1, 100 * MILLION, createActorData())
		scheduler.signalHandler.requestActingAfter(actor2, 200 * MILLION, createActorData())

		timeService.setTimeMillis(50)
		scheduler.systemSpeedCategory.systemSpeed.resume()
		verify(exactly = 1) { actor1.act(any(), any()) }
		verify(exactly = 0) { actor2.act(any(), any()) }

		timeService.setTimeMillis(80)
		scheduler.systemSpeedCategory.systemSpeed.resume()
		verify(exactly = 1) { actor1.act(any(), any()) }
		verify(exactly = 1) { actor2.act(any(), any()) }
	}

	@Test
	fun shouldPause() {
		val actor = createActor()
		scheduler.isActive = true
		scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())

		scheduler.systemSpeedCategory.systemSpeed.pause()
		timeService.setTimeMillis(150)

		verify(exactly = 0) { actor.act(any(), any()) }
	}

	@Test
	fun shouldResume() {
		val actor = createActor()
		scheduler.isActive = true
		scheduler.signalHandler.requestActingAfter(actor, 100 * MILLION, createActorData())

		scheduler.systemSpeedCategory.systemSpeed.pause()
		timeService.setTimeMillis(150)
		verify(exactly = 0) { actor.act(any(), any()) }

		scheduler.systemSpeedCategory.systemSpeed.resume()
		timeService.setTimeMillis(250)
		verify(exactly = 1) { actor.act(any(), any()) }
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

	@Test
	fun shouldActPrematurely() {
		val actor = createActor(propagationDelay = 100 * MILLION )
		val actorData = createActorData()

		scheduler.isActive = true
		scheduler.signalHandler.requestActingAfter(actor, actor.propagationDelay, actorData)
		timeService.setTimeMillis(50)

		scheduler.signalHandler.actPrematurely(actor, actorData)
		verify { actor.act(any(), any())}
	}

	/** ---- [SchedulerImplTest] */

	private fun createActor(isBreakpoint: Boolean = true, propagationDelay: Long = 0): Actor {
		val actor = spyk<ActorImpl>()
		actor.propagationDelay = propagationDelay
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

		override fun addActorListener(l: ActorListener) { }

		override fun removeActorListener(l: ActorListener) { }

		override fun executionInitialize(signalHandler: SignalHandler) { }

		override fun executionStart(signalHandler: SignalHandler) {
			state = ActorState.Idle
		}

		override fun act(signalHandler: SignalHandler, data: ActorData) {
			state = ActorState.Acting
			actingCalled = true
		}

		override fun actingVisualized(signalHandler: SignalHandler, l: ActorListener, data: ActorData?) { }

		override fun actingDone(signalHandler: SignalHandler, data: ActorData?) {
			state = ActorState.Idle
			signalHandler.requestActingAfter(target, targetPropDelay, createActorData())
		}

		override fun executionStopped(signalHandler: SignalHandler) {
			state = ActorState.NonExecuting
		}
	}
}