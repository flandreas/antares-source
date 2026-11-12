package io.antarescircuit.jabbah.execution.scheduler

import io.antarescircuit.jabbah.base.IssueImpl
import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.math.MILLION
import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.base.time.ControlledTimeService
import io.antarescircuit.jabbah.base.time.ControlledTimer
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.execution.ExecutionError
import io.antarescircuit.jabbah.execution.ExecutionTestRule
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.*
import io.antarescircuit.jabbah.execution.noise.NoNoiseGenerator
import io.antarescircuit.jabbah.execution.noise.NoiseGeneratorHolder
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import dev.mokkery.*
import dev.mokkery.answering.returns
import dev.mokkery.matcher.any
import dev.mokkery.verify.VerifyMode.Companion.exactly
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
		scheduler.requestActingAfter(actor, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)

		verify { actor.act(any(), any()) }
	}

	@Test
	fun shouldNotActBeforeDelay() {
		val actor = createActor()
		scheduler.isActive = true
		scheduler.requestActingAfter(actor, 200 * MILLION, createActorData())

		timeService.setTimeMillis(150)

		verify(exactly(0)) { actor.act(any(), any()) }
	}

	@Test
	fun shouldActAllWithSameDelay() {
		val actor1 = createActor()
		val actor2 = createActor()
		scheduler.isActive = true
		scheduler.requestActingAfter(actor1, 100 * MILLION, createActorData())
		scheduler.requestActingAfter(actor2, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)

		verify(exactly(1)) { actor1.act(any(), any()) }
		verify(exactly(1)) { actor2.act(any(), any()) }
	}

	@Test
	fun shouldForgetAfterActing() {
		val actor = createActor()
		scheduler.isActive = true
		scheduler.requestActingAfter(actor, 100 * MILLION, createActorData())
		timeService.setTimeMillis(300)

		scheduler.actingDone(actor, createActorData())

		verify(exactly(1)) { actor.act(any(), any()) }
		assertEquals(0, scheduler.numberOfRemainingSlots)
	}

	@Test
	fun shouldActOnlyOnceWhenRequestedTwice() {
		val actor = createActor()
		scheduler.isActive = true
		scheduler.requestActingAfter(actor, 100 * MILLION, createActorData())
		scheduler.requestActingAfter(actor, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)

		verify(exactly(1)) { actor.act(any(), any()) }
	}

	/** ---- Stepping tests */

	@Test
	fun shouldWaitInBreakpoint() {
		val actor = createActor()
		scheduler.isSoftBreakpointsEnabled = true
		scheduler.isSingleStepMode = true
		scheduler.isActive = true
		scheduler.requestActingAfter(actor, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)

		verify(exactly(0)) { actor.act(any(), any()) }
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
		scheduler.requestActingAfter(actor1, 100 * MILLION, createActorData())
		scheduler.requestActingAfter(actor2, 200 * MILLION, createActorData())

		timeService.setTimeMillis(150)
		scheduler.systemSpeedCategory.systemSpeed.resume()
		verify(exactly(1)) { actor1.act(any(), any()) }
		verify(exactly(0)) { actor2.act(any(), any()) }

		timeService.setTimeMillis(250)
		scheduler.systemSpeedCategory.systemSpeed.resume()
		verify(exactly(0)) { actor1.act(any(), any()) }
	}

	@Test
	fun shouldNotWaitForRealTimeWhenResuming() {
		val actor1 = createActor()
		val actor2 = createActor()
		scheduler.isSoftBreakpointsEnabled = true
		scheduler.isActive = true
		scheduler.isSingleStepMode = true
		scheduler.requestActingAfter(actor1, 100 * MILLION, createActorData())
		scheduler.requestActingAfter(actor2, 200 * MILLION, createActorData())

		timeService.setTimeMillis(50)
		scheduler.systemSpeedCategory.systemSpeed.resume()
		verify(exactly(1)) { actor1.act(any(), any()) }
		verify(exactly(0)) { actor2.act(any(), any()) }

		timeService.setTimeMillis(80)
		scheduler.systemSpeedCategory.systemSpeed.resume()
		verify(exactly(0)) { actor1.act(any(), any()) }
	}

	@Test
	fun shouldPause() {
		val actor = createActor()
		scheduler.isActive = true
		scheduler.requestActingAfter(actor, 100 * MILLION, createActorData())

		scheduler.systemSpeedCategory.systemSpeed.pause()
		timeService.setTimeMillis(150)

		verify(exactly(0)) { actor.act(any(), any()) }
	}

	@Test
	fun shouldResume() {
		val actor = createActor()
		scheduler.isActive = true
		scheduler.requestActingAfter(actor, 100 * MILLION, createActorData())

		scheduler.systemSpeedCategory.systemSpeed.pause()
		timeService.setTimeMillis(150)
		verify(exactly(0)) { actor.act(any(), any()) }

		scheduler.systemSpeedCategory.systemSpeed.resume()
		timeService.setTimeMillis(250)
		verify(exactly(1)) { actor.act(any(), any()) }
	}

	/** ---- Animation tests */

	@Test
	fun shouldWaitForActingDone() {
		val actor1: Actor = mock(MockMode.autofill)
		val actor2 = createActor()

		scheduler.isActive = true
		scheduler.requestActingAfter(actor1, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)
		verify(exactly(1)) { actor1.act(any(), any()) }
		verify(exactly(0)) { actor2.act(any(), any()) }

		resetCalls()
		timeService.setTimeMillis(300)
		verify(exactly(0)) { actor1.act(any(), any()) }
		verify(exactly(0)) { actor2.act(any(), any()) }
	}

	@Test
	fun shouldProcessActingDone() {
		val actor1 = createActor()
		val actor2 = ForwardingActor(actor1)

		scheduler.isActive = true
		scheduler.requestActingAfter(actor2, 100 * MILLION, createActorData())

		timeService.setTimeMillis(150)
		assertTrue(actor2.actingCalled)
		verify(exactly(0)) { actor1.act(any(), any()) }
		assertEquals(1, scheduler.numberOfRemainingSlots)

		scheduler.actingDone(actor2, createActorData())
		assertEquals(1, scheduler.numberOfRemainingSlots)

		timeService.setTimeMillis(250)
		verify(exactly(1)) { actor1.act(any(), any()) }
		scheduler.actingDone(actor1, createActorData())
		assertEquals(0, scheduler.numberOfRemainingSlots)
	}

	@Test
	fun shouldAnimateInParallel() {
		val actor1 = createActor()
		val actor2 = ForwardingActor(actor1)
		val actor3 = ForwardingActor(actor1)

		scheduler.isActive = true
		scheduler.requestActingAfter(actor2, 100 * MILLION, createActorData())
		scheduler.requestActingAfter(actor3, 100 * MILLION, createActorData())
		assertEquals(1, scheduler.numberOfRemainingSlots)

		timeService.setTimeMillis(150)
		verify(exactly(0)) { actor1.act(any(), any()) }
		assertTrue(actor2.actingCalled)
		assertTrue(actor3.actingCalled)

		scheduler.actingDone(actor2, createActorData())
		scheduler.actingDone(actor3, createActorData())
		assertEquals(1, scheduler.numberOfRemainingSlots)

		timeService.setTimeMillis(300)
		verify(exactly(1)) { actor1.act(any(), any()) }
	}

	@Test
	fun shouldActPrematurely() {
		val actor = createActor(propagationDelay = 100 * MILLION )
		val actorData = createActorData()

		scheduler.isActive = true
		scheduler.requestActingAfter(actor, actor.propagationDelay.value, actorData)
		timeService.setTimeMillis(50)

		scheduler.actPrematurely(actor, actorData)
		verify { actor.act(any(), any())}
	}

	@Test
	fun shouldExecuteManyPendingTimesInOrderAndCoalesceLatestData() {
		val manual = createManualScheduler()
		val calls = mutableListOf<ActorData>()
		val actor = object : ActorImpl() {
			override fun act(signalHandler: SignalHandler, data: ActorData) {
				calls.add(data)
				signalHandler.actingDone(this, data)
			}
		}
		manual.isActive = true
		val expected = List(200) { createActorData() }
		for (i in expected.indices.reversed()) {
			manual.requestActingAfter(actor, (i + 1).toLong(), createActorData())
			manual.requestActingAfter(actor, (i + 1).toLong(), expected[i])
		}
		assertEquals(expected.size, manual.numberOfRemainingSlots)
		repeat(expected.size) { manual.execute() }
		assertEquals(expected, calls)
		assertTrue(manual.isQueueEmpty)
		manual.dispose()
	}

	@Test
	fun shouldKeepNewSlotScheduledAtSameTimeAfterSynchronousCompletion() {
		val manual = createManualScheduler()
		val calls = mutableListOf<String>()
		val target = object : ActorImpl() {
			override fun act(signalHandler: SignalHandler, data: ActorData) {
				calls.add("target")
				signalHandler.actingDone(this, data)
			}
		}
		val source = object : ActorImpl() {
			override fun act(signalHandler: SignalHandler, data: ActorData) {
				calls.add("source")
				signalHandler.actingDone(this, data)
				signalHandler.requestActingAfter(target, 0, data)
			}
		}
		manual.isActive = true
		manual.requestActingAfter(source, 10, createActorData())
		manual.execute()
		assertEquals(1, manual.numberOfRemainingSlots)
		manual.execute()
		assertEquals(listOf("source", "target"), calls)
		assertTrue(manual.isQueueEmpty)
		manual.dispose()
	}

	@Test
	fun shouldRemovePrematureSlotsAndForgetSlotsOnRestart() {
		val manual = createManualScheduler()
		var calls = 0
		val actor = object : ActorImpl() {
			override fun act(signalHandler: SignalHandler, data: ActorData) {
				calls++
				signalHandler.actingDone(this, data)
			}
		}
		manual.isActive = true
		manual.requestActingAfter(createActor(), 10, createActorData())
		manual.requestActingAfter(actor, 20, createActorData())
		manual.actPrematurely(actor, null)
		assertEquals(1, manual.numberOfRemainingSlots)
		manual.requestActingAfter(actor, 20, createActorData())
		assertEquals(2, manual.numberOfRemainingSlots)
		manual.isActive = false
		manual.isActive = true
		manual.requestActingAfter(actor, 20, createActorData())
		assertEquals(1, manual.numberOfRemainingSlots)
		manual.execute()
		assertEquals(2, calls)
		assertTrue(manual.isQueueEmpty)
		manual.dispose()
	}

	@Test
	fun shouldNotLoseRequestMadeWhileActorIsStillActing() {
		val manual = createManualScheduler()
		val calls = mutableListOf<ActorData>()
		val second = createActorData()
		val actor = object : ActorImpl() {
			override fun act(signalHandler: SignalHandler, data: ActorData) {
				calls.add(data)
				// A zero-delay re-request while still acting, e.g. a changed input during a signal animation
				if (calls.size == 1) signalHandler.requestActingAfter(this, 0, second)
				signalHandler.actingDone(this, data)
			}
		}
		manual.isActive = true
		val first = createActorData()
		manual.requestActingAfter(actor, 10, first)

		while (!manual.isQueueEmpty) manual.execute()

		assertEquals(listOf(first, second), calls)
		manual.dispose()
	}

	@Test
	fun shouldReportFailingActorAndContinueWithLaterSlots() {
		val manual = createManualScheduler()
		val issues = mutableListOf<IssueImpl>()
		eventBus.register(IssueImpl::class) { issues.add(it) }
		var laterCalls = 0
		val failing = object : ActorImpl() {
			override fun act(signalHandler: SignalHandler, data: ActorData) = throw IllegalStateException("boom")
		}
		val later = object : ActorImpl() {
			override fun act(signalHandler: SignalHandler, data: ActorData) {
				laterCalls++
				signalHandler.actingDone(this, data)
			}
		}
		manual.isActive = true
		manual.requestActingAfter(failing, 10, createActorData())
		manual.requestActingAfter(later, 20, createActorData())

		manual.execute()
		manual.execute()

		assertEquals(1, laterCalls)
		assertTrue(manual.isQueueEmpty)
		assertEquals(1, issues.size)
		assertTrue(issues.single().description!!.contains("boom"))
		manual.dispose()
	}

	@Test
	fun shouldPropagateActorFailuresWhenNotContained() {
		val manual = SchedulerImpl(
			scheduler.systemSpeedCategory,
			timeService,
			eventBus,
			NoiseGeneratorHolder(NoNoiseGenerator(), eventBus),
			task = ManualSchedulerTask(),
			containActorFailures = false,
		).apply { isSingleStepMode = true }
		val failing = object : ActorImpl() {
			override fun act(signalHandler: SignalHandler, data: ActorData) = throw IllegalStateException("boom")
		}
		manual.isActive = true
		manual.requestActingAfter(failing, 10, createActorData())

		assertFailsWith<IllegalStateException> { manual.execute() }
		manual.dispose()
	}

	@Test
	fun shouldReportZeroDelayOscillationOnceWithInvolvedActors() {
		val manual = createManualScheduler()
		val issues = mutableListOf<IssueImpl>()
		eventBus.register(IssueImpl::class) { issues.add(it) }
		// Two actors re-triggering each other without delay, like cross-coupled zero-delay gates
		lateinit var b: ActorImpl
		val a = object : ActorImpl(id = 1) {
			override fun act(signalHandler: SignalHandler, data: ActorData) {
				signalHandler.actingDone(this, data)
				signalHandler.requestActingAfter(b, 0, data)
			}
		}
		b = object : ActorImpl(id = 2) {
			override fun act(signalHandler: SignalHandler, data: ActorData) {
				signalHandler.actingDone(this, data)
				signalHandler.requestActingAfter(a, 0, data)
			}
		}
		manual.isActive = true
		manual.requestActingAfter(a, 10, createActorData())

		repeat(SchedulerImpl.OSCILLATION_WAVE_LIMIT + 1_000) { manual.execute() }

		assertEquals(1, issues.size)
		val origin = issues.single().origin
		assertTrue(origin.contains("(1)") && origin.contains("(2)"), origin)
		assertEquals(10L, manual.executionTime)
		manual.dispose()
	}

	@Test
	fun shouldNotReportOscillationForDeepZeroDelayChain() {
		val manual = createManualScheduler()
		val issues = mutableListOf<IssueImpl>()
		eventBus.register(IssueImpl::class) { issues.add(it) }
		// A chain of 2000 zero-delay actors settles well below the oscillation limit
		val chain = ArrayList<ActorImpl>()
		repeat(2_000) { index ->
			chain.add(object : ActorImpl(id = index) {
				override fun act(signalHandler: SignalHandler, data: ActorData) {
					signalHandler.actingDone(this, data)
					chain.getOrNull(index + 1)?.let { signalHandler.requestActingAfter(it, 0, data) }
				}
			})
		}
		manual.isActive = true
		manual.requestActingAfter(chain.first(), 10, createActorData())
		while (!manual.isQueueEmpty) manual.execute()

		assertTrue(issues.isEmpty())
		manual.dispose()
	}

	private fun createManualScheduler(): SchedulerImpl = SchedulerImpl(
		scheduler.systemSpeedCategory,
		timeService,
		eventBus,
		NoiseGeneratorHolder(NoNoiseGenerator(), eventBus),
		task = ManualSchedulerTask(),
	).apply { isSingleStepMode = true }

	/** ---- [SchedulerImplTest] */

	private fun createActor(isBreakpoint: Boolean = true, propagationDelay: Long = 0): Actor {
		val actor = spy<Actor>(ActorImpl())
		actor.propagationDelay = LongValueImpl(propagationDelay)
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

		override var propagationDelay: LongValue = LongValueImpl(100L)

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