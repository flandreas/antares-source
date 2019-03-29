package ch.scorpion.jabbah.execution.scheduler

import ch.scorpion.jabbah.base.Status
import ch.scorpion.jabbah.base.StatusType
import ch.scorpion.jabbah.base.collection.PriorityQueue
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.ActionListener
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.TimeService
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.SystemSpeedEvent
import ch.scorpion.jabbah.execution.issue.IssueCollectorEvent
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

/**
 * Standard implementation of the [Scheduler] interface.
 */
class SchedulerImpl(
	private val timeService: TimeService = BaseModule.timeService,
	timer: Timer = System.get().createTimer(),
	private val eventBus: EventBus = BaseModule.eventBus,
	private val noiseGeneratorHolder: NoiseGeneratorHolder = ExecutionModule.noiseGeneratorHolder,
	private val currentSystemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory
) : Scheduler {

	companion object {
		/** The custom name [String] of the limit [SystemSpeedCategory] in [Properties] for sending [SchedulerEvent]s.*/
		const val PROP_SCHEDULER_EVENT_SYSTEM_SPEED_LIMIT = "execution.scheduler.eventSystemSpeedLimit"

		private val LOG by logger(SchedulerImpl::class)
		private const val SETTING_EXECUTION_DEPTH = "execution.scheduler.deepExecution"
		private const val SETTING_STOP_ON_ISSUE = "execution.scheduler.stopOnIssue"
	}

	/** The queue of pending [Slot]s ordered by ascending execution time.*/
	private val queue = PriorityQueue<Slot>()

	/** The object that is repeatedly called by the specified [Timer] in order to perform execution steps.*/
	private val task = Task(timer)

	/** Determines whether this [Scheduler] is active or not.*/
	private var activationState: SchedulerActivationState = SchedulerActivationState.PASSIVE

	/** Determines whether this [Scheduler] is currently running or paused (single step mode).*/
	private var runningState: SchedulerRunningState = SchedulerRunningState.RUNNING

	/** The relative time [in ns] since execution has been started. */
	private var relativeTime: Long = 0

	/** Holds the absolute real time in nanoseconds of when the execution has been started.*/
	private var realStartTime: Long = 0

	init {
		eventBus.register(SystemSpeedEvent::class) { task.adaptToSystemSpeed() }
		eventBus.register(IssueCollectorEvent::class) {
			if (isActive && isStopOnIssue && it.issue != null) {
				System.get().invokeLater {
					isActive = false
					LOG.debug("SchedulerImpl: execution stopped due to Issue '${it.issue.name}'")
					eventBus.post(ExecutionStoppedOnIssueEvent(this))
				}
			}
		}
	}

	/** ---- [Scheduler] interface */

	override val signalHandler: SignalHandler get() = this

	override val numberOfRemainingSlots: Int get() = queue.size

	override var isActive: Boolean
		get() = activationState == SchedulerActivationState.ACTIVE
		set(value) {
			if (value == isActive) {
				return
			}
			if (value) {
				start()
			} else {
				stop()
			}
			Status.set(StatusType.Small, null)
		}

	override var isPaused: Boolean
		get() = runningState == SchedulerRunningState.PAUSED
		set(value) {
			if (value == isPaused) {
				return
			}
			if (value) {
				runningState = SchedulerRunningState.PAUSED
				task.stop()
			} else {
				runningState = SchedulerRunningState.RUNNING
				task.startIfNeeded()
			}
			eventBus.post(SchedulerRunningStateEvent(this))
		}

	override var isStopOnIssue: Boolean = BaseModule.settings.getBoolean(SETTING_STOP_ON_ISSUE, true)
		set(value) {
			if (field == value) {
				return
			}
			field = value
			BaseModule.settings.set(SETTING_STOP_ON_ISSUE, field)
			eventBus.post(StopOnIssueEvent(this, field))
		}

	override var isSimulationTimeStatusEnabled: Boolean = false
		set(value) {
			if (field == value) {
				return
			}
			field = value
			if (field) {
				publishSimulationTimeStatus()
			} else {
				clearSimulationTimeStatus()
			}
			eventBus.post(SimulationTimeStatusEnabledEvent(this))
		}

	override fun step() {
		if (!isActive) {
			throw IllegalStateException("cannot step when not active")
		}
		if (!isPaused) {
			throw IllegalStateException("cannot step when not paused")
		}
		do {
			val result = executionStep()
		} while (result.recalculated && !result.breakpoint)
	}

	override fun proceedTo(time: Long) {
		while (!queue.isEmpty && executionTime < time) {
			executionStep()
		}
	}

	override fun printSchedule() {
		LOG.debug("SchedulerImpl: Scheduling queue at $relativeTime ns")
		queue.elements().forEach { it.print() }
	}

	/** ---- [SignalHandler] interface */

	override var isDeepExecution: Boolean = BaseModule.settings.getBoolean(SETTING_EXECUTION_DEPTH, true)
		set(value) {
			if (field == value) {
				return
			}
			field = value
			BaseModule.settings.set(SETTING_EXECUTION_DEPTH, field)
			eventBus.post(ExecutionDepthEvent(this, field))
		}

	override val executionTime: Long get() = relativeTime

	override fun logTrace(clazz: KClass<*>, id: Int, msg: () -> String) {
		// TEST BEGIN
		if (LOG.isTraceEnabled()) {
			LOG.trace("$executionTime ns [${clazz.simpleName} ($id)]: ${msg.invoke()}")
		}
		// TEST
	}

	override fun requestActingAfter(actor: Actor, delay: Long, data: ActorData) {
		requestActingImpl(actor, delay, false, data)
	}

	override fun requestActingTimeFreeze(actor: Actor, data: ActorData) {
		requestActingImpl(actor, 1, true, data)
	}

	override fun actingDone(actor: Actor) {
		logTrace(System.get().getClass(actor), actor.id) { "Acting done" }
		val slot = queue.peek()
		if (slot != null) {
			val request = slot.findRequest(actor)
			if (request != null && request.isActing) {
				actor.actingDone(this, request.actorData)
			}
			slot.actingDone(actor)
			if (slot.isDone) {
				removeSlot(slot)
				postSchedulerStateEvent()
			}
		}
	}

	private fun requestActingImpl(actor: Actor, delay: Long, timeFreeze: Boolean, data: ActorData) {
		if (!isActive) {
			return
		}
		logTrace(System.get().getClass(actor), actor.id) { "Request to act after $delay ns" }
		if (delay == 0L) {
			actor.act(this, data)
		} else {
			// TODO Implement adaptive Task scheduling (i.e. vary the time between ticks)
			val schedulingTime = executionTime + delay + noiseGeneratorHolder.current.noise(10)
			val slot = getSlotAt(schedulingTime)
			if (slot != null) {
				slot.timeFreeze = slot.timeFreeze || timeFreeze
				slot.addActor(actor, data)
			} else {
				addSlot(Slot(schedulingTime, timeFreeze, actor, data))
				postSchedulerStateEvent()
			}
			task.startIfNeeded()
		}
		postSchedulerEvent(actor, SchedulerEvent.Type.REQUESTED)
	}

	/** ---- [SchedulerImpl] */

	private fun postSchedulerEvent(actor: Actor, type: SchedulerEvent.Type) {
		// Is only active when exploring the system. For performance reasons, we therefore avoid sending
		// unnecessary (and costly) events.
		if (isActive && currentSystemSpeedCategory.systemSpeedCategory >= SystemSpeedCategory.withName(BaseModule.properties.getString(PROP_SCHEDULER_EVENT_SYSTEM_SPEED_LIMIT))) {
			eventBus.post(SchedulerEvent(type, this, actor))
		}
	}

	private fun postSchedulerStateEvent() {
		eventBus.post(SchedulerStateEvent(numberOfRemainingSlots = queue.size, relativeTime = relativeTime))
		if (isSimulationTimeStatusEnabled || runningState == SchedulerRunningState.PAUSED) {
			publishSimulationTimeStatus()
		}
	}

	private fun publishSimulationTimeStatus() {
		Status.set(StatusType.Small, "$relativeTime ns")
	}

	private fun clearSimulationTimeStatus() {
		Status.set(StatusType.Small, null)
	}

	private fun addSlot(slot: Slot) {
		LOG.trace("Add slot at ${slot.relativeTime}")
		queue.add(slot)
	}

	/** Removes the [Slot] at the head of the queue.*/
	private fun removeSlot(slot: Slot) {
		if (!queue.isEmpty) {
			queue.remove(slot)
			if (queue.isEmpty) {
				task.stop()
			}
		}
	}

	/** Returns the [Slot] in the queue at the specified relative execution time, if any.*/
	private fun getSlotAt(relativeTime: Long): Slot? {
		return queue.elements().find { it.relativeTime == relativeTime }
	}

	/** Resets the [Scheduler].*/
	private fun reset() {
		updateRelativeTime(0)
		queue.clear()
	}

	private fun start() {
		LOG.debug("Scheduler started")
		reset()
		realStartTime = timeService.nowNanos()
		activationState = SchedulerActivationState.ACTIVE
		eventBus.post(SchedulerActivationStateEvent(this))
		task.startIfNeeded()
	}

	private fun stop() {
		LOG.debug("Scheduler stopped")
		task.stop()
		activationState = SchedulerActivationState.PASSIVE
		eventBus.post(SchedulerActivationStateEvent(this))
		reset()
	}

	private fun updateRelativeTime(relativeTime: Long) {
		this.relativeTime = relativeTime
	}

	private fun getRelativeRealTime(): Long = timeService.nowNanos() - realStartTime

	private fun getNextExecutableSlot(): Slot? {
		val slot = queue.peek()
		if (slot == null || !slot.isExecutable) {
			return null
		}
		return slot
	}

	/** Executes all [Request]s of the next executable [Slot].*/
	private fun executionStep(): ExecutionStepResult {
		val slot: Slot = getNextExecutableSlot() ?: return ExecutionStepResult(recalculated = false, breakpoint = false)

		LOG.trace("Execution step at $relativeTime ns, queue size is ${queue.size}")

		// Resynchronize relative time with real time
		if (!slot.timeFreeze) {
			updateRelativeTime(getRelativeRealTime())
		}

		var recalculated = false
		var breakpoint = false

		// When stepping, we don't wait for real time. When not in stepping mode, we wait until
		// real time has reached the simulation time, so that [Actors] like slow timers have a chance
		// to seem to behave in real time.

		if (isPaused || slot.relativeTime <= relativeTime) {
			updateRelativeTime(slot.relativeTime)
			slot.getRequests().filter { it.isActable }.forEach {
				if (LOG.isTraceEnabled()) {
					logTrace(System.get().getClass(it.actor), it.actor.id) { "Executing" }
				}
				breakpoint = breakpoint || it.actor.isBreakpoint
				if (it.actor.act(this@SchedulerImpl, it.actorData)) {
					it.setDone()
				} else {
					it.setActing()
				}
			}

			if (slot.isDone) {
				removeSlot(slot)
			}
			recalculated = true
		} else {
			updateRelativeTime(min(getRelativeRealTime(), slot.relativeTime))
		}
		postSchedulerStateEvent()
		return ExecutionStepResult(recalculated, breakpoint)
	}

	/** Repeatedly called by the [Timer] in order to perform an execution step.*/
	private inner class Task(private val timer: Timer) : ActionListener {

		private val SLOWDOWN_FACTOR = 0.5

		init {
			timer.initialize(calculateTimerInterval()) { actionPerformed(it) }
		}

		/**
		 * Called by the [Timer] that drives this [Task].
		 *
		 * Due to the types in the interface of a [Timer], the interval of a [Timer] can't be smaller than 1 ms.
		 * If the system should run at maximum speed, this interval is too long. We therefore perform more than
		 * one execution step at a single timer tick.
		 */
		override fun actionPerformed(event: ActionEvent) {
			if (currentSystemSpeedCategory.systemSpeed.isMaximum) {
				val beginTime = System.get().currentTimeMillis()
				while (!queue.isEmpty && System.get().currentTimeMillis() - beginTime < 20) {
					executionStep()
				}
			} else {
				var count = 0
				while (count < 10 && executionStep().recalculated) {
					count++
				}
			}
		}

		fun adaptToSystemSpeed() {
			timer.interval = calculateTimerInterval()
		}

		private fun calculateTimerInterval(): Int {
			val interval = max(1.0, SLOWDOWN_FACTOR * (SystemSpeed.MAX_SPEED - currentSystemSpeedCategory.systemSpeed.speed)).toInt()
			LOG.debug("SchedulerImpl: interval = $interval")
			return interval
		}

		fun startIfNeeded() {
			if (!isPaused && !queue.isEmpty && !timer.isRunning()) {
				LOG.trace("Starting timer")
				timer.start()
			}
		}

		fun stop() {
			LOG.trace("Stopping timer")
			timer.stop()
		}
	}

	/**
	 * Represents the result of a single execution step performed by [Task].
	 * @property recalculated `true`if at least one [Actor] has been recalculated
	 * @property breakpoint TODO Documentation
	 */
	private data class ExecutionStepResult(val recalculated: Boolean, val breakpoint: Boolean)

	/**
	 * A [Slot] is an entry in the queue of the [Scheduler] and contains all [Request]s of [Actor]s
	 * that should be scheduled at a particular [relativeTime].
	 *
	 * The primary constructor creates a new [Slot] with the specified [Actor] as its first [Request].
	 *
	 * @property relativeTime the relative execution time (in ns) at which `actor` is to be scheduled
	 * @property timeFreeze If this flag is `true`, the [Scheduler] should not increase the simulation time when processing
	 * this [Slot]. This is used for supporting animated [Actor]s, which might take a
	 * long time until their acting is done, but this time should not be accounted as execution time, because
	 * if it would, other (faster) [Actor]s would pass by, which is not desired.
	 * @param actor the [Actor] for which the first [Request] is added to this [Slot]
	 * @paran data the [ActorData] of the first [Request] to be added to this [Slot]
	 */
	private inner class Slot(
		val relativeTime: Long,
		var timeFreeze: Boolean,
		actor: Actor,
		data: ActorData
	) : Comparable<Slot> {

		val isExecutable: Boolean get() = requests.any { it.isActable }

		val isDone: Boolean get() = requests.all { it.isDone }

		/**
		 * Contains the [Actor]s to the scheduled at the specified [relativeTime]. A particular [Actor]
		 * can only be contained at most once.
		 */
		private val requests = mutableListOf<Request>()


		init {
			requests.add(Request(actor, data))
		}

		override fun compareTo(other: Slot): Int = relativeTime.compareTo(other.relativeTime)

		fun getRequests(): Iterable<Request> = requests

		/**
		 * Adds the specified [Actor] and its [ActorData] as a new [Request] to this [Slot].
		 * If the [Actor] is already present, its [ActorData] is updated.
		 * */
		fun addActor(actor: Actor, data: ActorData) {
			val request = findRequest(actor)
			if (request != null) {
				request.actorData = data
			} else {
				requests.add(Request(actor, data))
			}
		}

		fun actingDone(actor: Actor) {
			findRequest(actor)?.setDone()
		}

		fun findRequest(actor: Actor): Request? = requests.find { it.actor === actor }

		/** Prints this [Slot] to the DEBUG log.*/
		fun print() {
			LOG.debug("\tSlot at $relativeTime ns with ${requests.size} requests, timeFreeze=$timeFreeze")
			requests.forEach { it.print() }
		}
	}

	private inner class Request(
		val actor: Actor,
		var actorData: ActorData
	) {

		private var _isActing: Boolean = false

		private var _isDone: Boolean = false

		/** `true` if [actor] has already been asked to execute*/
		val isActing: Boolean get() = _isActing

		/** `true` if [actor] has already answered with `done`.*/
		val isDone: Boolean get() = _isDone

		val isActable: Boolean get() = !isActing && !isDone

		fun setActing() {
			_isActing = true
		}

		fun setDone() {
			if (_isDone) {
				return
			}
			_isDone = true
			postSchedulerEvent(actor, SchedulerEvent.Type.DONE)
		}

		/** Prints this [Request] to the DEBUG log.*/
		fun print() {
			LOG.debug("\t\tRequest for ${actor::class.simpleName} with ID ${actor.id}, isActing=$_isActing, isDone=$_isDone")
		}
	}
}