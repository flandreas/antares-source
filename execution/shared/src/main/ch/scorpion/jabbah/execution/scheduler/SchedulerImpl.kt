package ch.scorpion.jabbah.execution.scheduler

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.collection.PriorityQueue
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.SystemSpeedPauseEvent
import ch.scorpion.jabbah.base.time.TimeService
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.ExecutionErrorHandler
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.execution.issue.IssueCollectorEvent
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import ch.scorpion.jabbah.execution.scheduler.ExecutionStepResult.Companion.NOT_RECALCULATED_BREAKPOINT
import ch.scorpion.jabbah.execution.scheduler.ExecutionStepResult.Companion.NOT_RECALCULATED_NO_BREAKPOINT
import ch.scorpion.jabbah.execution.scheduler.ExecutionStepResult.Companion.RECALCULATED_NO_BREAKPOINT
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationState.ACTIVE
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationState.PASSIVE
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategoryEvent
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

/**
 * Standard implementation of the [Scheduler] interface.
 */
class SchedulerImpl(
	private val currentSystemSpeedCategory: CurrentSystemSpeedCategory,
	private val timeService: TimeService = BaseModule.timeService,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val noiseGeneratorHolder: NoiseGeneratorHolder = ExecutionModule.noiseGeneratorHolder,
	private val task: SchedulerTask = ExecutionModule.schedulerTaskFactory.invoke(currentSystemSpeedCategory, eventBus),
	private val executionErrorHandler: ExecutionErrorHandlerImpl = ExecutionErrorHandlerImpl(),
	isDeepExecution: Boolean = true
) : Scheduler, ExecutionErrorHandler by executionErrorHandler {

	companion object {

		/** The custom name [String] of the limit [SystemSpeedCategory] in [Properties] for sending [SchedulerEvent]s.*/
		const val PROP_SCHEDULER_EVENT_SYSTEM_SPEED_LIMIT = "execution.scheduler.eventSystemSpeedLimit"

		private val LOG by logger(SchedulerImpl::class)
		private const val SETTING_STOP_ON_ISSUE = "execution.scheduler.stopOnIssue"
		private const val SETTING_ENABLE_SOFT_BREAKPOINTS = "execution.scheduler.enableSoftBreakpoints"
		private const val SETTING_ENABLE_SIMULATION_TIME_STATUS_BAR = "execution.scheduler.enableSimulationTimeStatusBar"
	}

	/** The queue of pending [Slot]s ordered by ascending execution time.*/
	private val queue = PriorityQueue<Slot>()

	/** Determines whether this [Scheduler] is active or not.*/
	private var activationState: SchedulerActivationState = PASSIVE

	/** The relative time [in ns] since execution has been started. */
	private var relativeTime: Long = 0

	private val formattedRelativeTime: String get() = StringUtils.formatLong(relativeTime)

	/** Holds the absolute real time in nanoseconds of when the execution has been started.*/
	private var realStartTime: Long = 0

	/** The time (ns) when execution has been paused. Used to "freeze" real time while [SystemSpeed.isPaused] is `true`.*/
	private var pauseTime: Long = 0

	/** The duration (ns) while execution was paused. Used to "freeze" real time while [SystemSpeed.isPaused] is `true` */
	private var accumulatedPauseTime: Long = 0

	private val issueCollectorListener: EventHandler<IssueCollectorEvent> = {
		if (isActive && isStopOnIssue && it.issue != null) {
			systemSpeedCategory.systemSpeed.pause()
			System.invokeLater {
				LOG.trace("execution stopped due to Issue '${it.issue.name}'")
				eventBus.post(ExecutionStoppedOnIssueEvent(it.issue, this))
			}
		}
	}

	private val breakListener: EventHandler<BreakEvent> = {
		hardBreakpointReceived = true
		if (isSingleStepMode) {
			currentSystemSpeedCategory.systemSpeed.pause()
		}
	}

	private val pauseEventListener: EventHandler<SystemSpeedPauseEvent> = {
		if (isActive && it.source === currentSystemSpeedCategory.systemSpeed) {
			if (it.isPaused) {
				handlePaused()
			} else {
				handleResumed()
			}
		}
	}

	private val systemSpeedCategoryHandler: EventHandler<SystemSpeedCategoryEvent> = {
		if (it.source === currentSystemSpeedCategory) {
			if (!displaySimulationTime) {
				clearSimulationTimeStatus()
			} else {
				publishSimulationTimeStatus()
			}
		}
	}

	private val displaySimulationTime: Boolean get() =
		isSingleStepMode || isSimulationTimeStatusEnabled && currentSystemSpeedCategory.systemSpeedCategory >= SystemSpeedCategory.Observe

	init {
		task.bind(this)

		eventBus.register(IssueCollectorEvent::class, issueCollectorListener)
		eventBus.register(BreakEvent::class, breakListener)
		eventBus.register(SystemSpeedPauseEvent::class, pauseEventListener)
		eventBus.register(SystemSpeedCategoryEvent::class, systemSpeedCategoryHandler)
	}

	override fun dispose() {
		eventBus.unregister(issueCollectorListener)
		eventBus.unregister(breakListener)
		eventBus.unregister(pauseEventListener)
		eventBus.unregister(systemSpeedCategoryHandler)
	}

	/** ---- [Scheduler] interface */

	override val systemSpeedCategory: CurrentSystemSpeedCategory get() = currentSystemSpeedCategory

	override val numberOfRemainingSlots: Int get() = queue.size

	override val isQueueEmpty: Boolean get() = queue.isEmpty

	override val hasDeferredExecutionError: Boolean get() = executionErrorHandler.executionErrorCount > 0

	override var isActive: Boolean
		get() = activationState == ACTIVE
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

	override var isSingleStepMode: Boolean = false
		set(value) {
			if (value != field) {
				field = value
				eventBus.post(SchedulerSingleStepModeEvent(this))
				startTaskIfNeeded()
			}
		}

	/**
	 * Set to `true` if an [Actor] requests a breakpoint, but the system has to defer the breakpoint
	 * until all running signal flow animations of the current [Slot] have been completed. Otherwise,
	 * slight differences in the duration of these animations would lead to pausing slower animations
	 * on their way, which is not desired.
	 */
	private var isInBreakpointPending: Boolean = false

	override var isInBreakpoint: Boolean = false
		private set(value) {
			if (field == value) {
				return
			}
			field = value
			if (field) {
				isInBreakpointPending = false
				currentSystemSpeedCategory.systemSpeed.pause()
			}
			LOG.trace("isInBreakpoint is $field")
			eventBus.post(BreakpointEvent(this))
		}

	private var hardBreakpointReceived: Boolean = false

	override var isStopOnIssue: Boolean = BaseModule.settings.getBoolean(SETTING_STOP_ON_ISSUE, true)
		set(value) {
			if (field == value) {
				return
			}
			field = value
			BaseModule.settings.set(SETTING_STOP_ON_ISSUE, field)
			eventBus.post(StopOnIssueEvent(this, field))
		}

	override var isSimulationTimeStatusEnabled: Boolean = BaseModule.settings.getBoolean(
		SETTING_ENABLE_SIMULATION_TIME_STATUS_BAR, false)
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
			BaseModule.settings.set(SETTING_ENABLE_SIMULATION_TIME_STATUS_BAR, field)
			eventBus.post(SimulationTimeStatusEnabledEvent(this))
		}

	override var isSoftBreakpointsEnabled: Boolean = BaseModule.settings.getBoolean(SETTING_ENABLE_SOFT_BREAKPOINTS, false)
		set(value) {
			if (field == value) {
				return
			}
			field = value
			BaseModule.settings.set(SETTING_ENABLE_SOFT_BREAKPOINTS, field)
			eventBus.post(EnableSoftBreakpointsEvent(this))
		}

	override var softBreakpointsArmTime: Long = 0

	override fun execute(): ExecutionStepResult {
		return executeImpl(resume = false)
	}

	override fun proceedTo(time: Long, maxIteration: Int) {
		LOG.trace("Proceed to ${StringUtils.formatLong(time)} ns")
		var i = 0
		while (!queue.isEmpty && executionTime < time && i < maxIteration) {
			execute()
			i++
		}
		// Repeat because time freezing slots update relative time at the end of executionStep
		execute()
	}

	override fun printSchedule() {
		LOG.info("Scheduling queue at $formattedRelativeTime ns")
		queue.elements().forEach { it.print() }
	}

	/** ---- [SignalHandler] interface */

	override var isDeepExecution: Boolean = isDeepExecution

	override val executionTime: Long get() = relativeTime

	override var executionContext: Any? = null

	override val isLogTrace: Boolean get() = LOG.isTraceEnabled()

	override fun logTrace(clazz: KClass<*>, id: Int, msg: () -> String) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("${StringUtils.formatLong(executionTime)} ns [${clazz.simpleName} ($id)]: ${msg.invoke()}")
		}
	}

	override fun logActorTrace(actor: Actor, msg: () -> String) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("${StringUtils.formatLong(executionTime)} ns [${System.getClass(actor).simpleName} (${actor.id})]: ${msg.invoke()}")
		}
	}

	override fun requestActingAfter(actor: Actor, delay: Long, data: ActorData) {
		requestActingImpl(actor, delay, data)
	}

	override fun actPrematurely(actor: Actor, data: ActorData?) {
		val slot = getSlotWithRequestForActor(actor)
		if (slot != null) {
			val request = slot.findRequest(actor)!!
			if (request.isActable) {
				logActorTrace(request.actor) { "Executing prematurely" }
				request.act()
			}
		}
	}

	override fun actingDone(actor: Actor, data: ActorData?) {
		logActorTrace(actor) { "Acting done" }
		var slot = queue.peek()
		if (slot != null) {
			var request = slot.findRequest(actor)

			// Needed for processing 'actPrematurely'
			if (request == null) {
				slot = getSlotWithRequestForActor(actor)
				if (slot != null) {
					request = slot.findRequest(actor)
				}
			}

			if (slot != null) {
				if (request != null && request.isActing) {
					actor.actingDone(this, request.actorData)
				}
				slot.actingDone(actor, request)

				if (isInBreakpointPending && !slot.isActing) {
					isInBreakpoint = true
				}

				if (slot.empty) {
					removeSlot(slot)
					postSchedulerStateEvent()
				}
			} else {
				actor.actingDone(this, data)
			}
		} else {
			actor.actingDone(this, data)
		}
	}

	private fun requestActingImpl(actor: Actor, delay: Long, data: ActorData) {
		if (!isActive) {
			return
		}
		if (LOG.isTraceEnabled()) {
			logActorTrace(actor) { "Request to act after ${StringUtils.formatLong(delay)} ns" }
		}
		// TODO Implement adaptive Task scheduling (i.e. vary the time between ticks)
		val schedulingTime = executionTime + delay + noiseGeneratorHolder.current.noise(10)
		val slot = getSlotAt(schedulingTime)
		if (slot != null) {
			slot.addActor(actor, data)
		} else {
			addSlot(Slot(schedulingTime, actor, data))
			postSchedulerStateEvent()
		}
		startTaskIfNeeded()

		postSchedulerEvent(actor, SchedulerEvent.Type.REQUESTED)
	}

	/** ---- [SchedulerImpl] */

	private fun handlePaused() {
		task.stop()
		pauseTime = timeService.nowNanos()
	}

	private fun handleResumed() {
		if (!isActive) {
			return
		}
		accumulatedPauseTime += (timeService.nowNanos() - pauseTime)
		executeImpl(resume = true)
		hardBreakpointReceived = false
		startTaskIfNeeded()
	}

	/**
	 * Proceeds the pushing time further and completing waiting [Actor]s until the queue is empty.
	 * Used only in testing for passing boot strapping activities before the real test can begin.
	 * Should only be used for test scenarios without cyclic [Actor] dependencies.
	 */
	fun proceedUntilQueueIsEmpty(
		timeService: ControlledTimeService,
		actorListener: ActorListener,
		maxIteration: Int = 1_000
	) {
		var i = 0
		while (!queue.isEmpty) {
			val slot = queue.peek()
			slot!!.getRequests().forEach {
				if (it.isActing) {
					it.actor.actingVisualized(this, actorListener)
				}
			}
			timeService.setTimeNanos(slot.relativeTime)
			execute()
			i++
			if (i > maxIteration) {
				throw IllegalStateException("max iteration count exceeded")
			}
		}
	}

	private fun postSchedulerEvent(actor: Actor, type: SchedulerEvent.Type) {
		// Is only active when exploring the system. For performance reasons, we therefore avoid sending
		// unnecessary (and costly) events.
		if (isActive && currentSystemSpeedCategory.systemSpeedCategory >= SystemSpeedCategory.withName(BaseModule.properties.getString(PROP_SCHEDULER_EVENT_SYSTEM_SPEED_LIMIT))) {
			eventBus.post(SchedulerEvent(type, this, actor))
		}
	}

	private fun postSchedulerStateEvent() {
		if (displaySimulationTime) {
			publishSimulationTimeStatus()
		}
	}

	private fun publishSimulationTimeStatus() {
		Status.set(StatusType.Small, "${StringUtils.formatLong(relativeTime)} ns")
	}

	private fun clearSimulationTimeStatus() {
		Status.set(StatusType.Small, null)
	}

	private fun addSlot(slot: Slot) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("Add slot ${StringUtils.formatLong(slot.relativeTime)}")
		}
		queue.add(slot)
	}

	/** Removes the [Slot] at the head of the queue.*/
	private fun removeSlot(slot: Slot) {
		if (!queue.isEmpty) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Remove slot ${StringUtils.formatLong(slot.relativeTime)}")
			}
			queue.remove(slot)
			if (queue.isEmpty) {
				task.stop()
			}
		}
		executionErrorHandler.reevaluateExecutionErrors(queue.isEmpty, this)
	}

	/** Returns the [Slot] in the queue at the specified relative execution time, if any.*/
	private fun getSlotAt(relativeTime: Long): Slot? {
		return queue.elements().find { it.relativeTime == relativeTime }
	}

	private fun getSlotWithRequestForActor(actor: Actor): Slot? =
		queue.elements().firstOrNull { it.findRequest(actor) != null }

	/** Resets the [Scheduler].*/
	private fun reset() {
		relativeTime = 0
		softBreakpointsArmTime = 0
		queue.clear()
		executionErrorHandler.reset()
	}

	private fun start() {
		LOG.trace("Scheduler started")
		reset()
		realStartTime = timeService.nowNanos()
		pauseTime = 0
		accumulatedPauseTime = 0
		currentSystemSpeedCategory.systemSpeed.resume()
		isInBreakpoint = false
		isInBreakpointPending = false
		hardBreakpointReceived = false
		activationState = ACTIVE
		startTaskIfNeeded()
		eventBus.post(SchedulerActivationStateEvent(this))
	}

	private fun startTaskIfNeeded() {
		if (!currentSystemSpeedCategory.systemSpeed.isPaused) {
			task.startIfNeeded()
		}
	}

	private fun stop() {
		LOG.trace("Scheduler stopped")
		task.stop()
		activationState = PASSIVE
		isInBreakpoint = false
		eventBus.post(SchedulerActivationStateEvent(this))
		reset()
	}

	private fun updateRelativeTime(relativeTime: Long) {
		val newRelativeTime = max(relativeTime, this.relativeTime)
		if (newRelativeTime > this.relativeTime) {
			this.relativeTime = newRelativeTime
			if (LOG.isTraceEnabled()) {
				LOG.trace("${StringUtils.formatLong(executionTime)} ns Updated relative time")
			}
			executionErrorHandler.reevaluateExecutionErrors(queue.isEmpty, this)
		}
	}

	private fun getRelativeRealTime(): Long = timeService.nowNanos() - realStartTime - accumulatedPauseTime

	/**
	 * Executes all [Request]s of the next executable [Slot] if not suspended by a breakpoint.
	 * @param resume `true` if the current breakpoint has already been handled, and the execution is to be resumed
	 */
	private fun executeImpl(resume: Boolean): ExecutionStepResult {
		val optionalSlot = queue.peek()
		if (optionalSlot == null) {
			updateRelativeTime(getRelativeRealTime())
			return NOT_RECALCULATED_NO_BREAKPOINT
		}

		val slot: Slot = optionalSlot

		if (!slot.isExecutable) {
			updateRelativeTime(slot.relativeTime)
			// TODO Can we pause the Timer here?
			return NOT_RECALCULATED_NO_BREAKPOINT
		}

		// LOG.trace("Execution step at $formattedRelativeTime ns, queue size is ${queue.size}")

		// Resynchronize relative time with real time (if simulation is faster that real time)
		updateRelativeTime(min(slot.relativeTime, getRelativeRealTime()))

		var recalculated = false

		// When stepping, we don't wait for real time. When not in stepping mode, we wait until
		// real time has reached the simulation time, so that [Actors] like slow timers have a chance
		// to seem to behave in real time.

		if (isSingleStepMode || slot.relativeTime <= relativeTime) {
			updateRelativeTime(slot.relativeTime)

			// Check for a breakpoint. If any of the Actors requests a break, skip the entire
			// slot and continue only upon the next resume.
			if (!resume && checkForBreakpoint(slot)) {
				// LOG.trace("Stop task because breakpoint detected")
				task.stop()
				if (slot.isActing) {
					// Defer pausing until all acting Actors are done
					isInBreakpointPending = true
				} else {
					isInBreakpoint = true
				}
				return NOT_RECALCULATED_BREAKPOINT
			}

			isInBreakpoint = false

			slot.getRequests().filter { it.isActable }.forEach {
				// logActorTrace(it.actor) { "Executing" }
				it.act()
			}

			if (slot.empty) {
				removeSlot(slot)
			}
			recalculated = true
		} else {
			updateRelativeTime(min(getRelativeRealTime(), slot.relativeTime))
		}
		postSchedulerStateEvent()

		return if (recalculated) RECALCULATED_NO_BREAKPOINT else NOT_RECALCULATED_NO_BREAKPOINT
	}

	private fun checkForBreakpoint(slot: Slot): Boolean =
		isSingleStepMode
			&& executionTime > softBreakpointsArmTime
			&&(hardBreakpointReceived || isSoftBreakpointsEnabled && slot.getRequests().any { it.isActable && it.actor.isBreakpoint })

	/**
	 * A [Slot] is an entry in the queue of the [Scheduler] and contains all [Request]s of [Actor]s
	 * that should be scheduled at a particular [relativeTime].
	 *
	 * The primary constructor creates a new [Slot] with the specified [Actor] as its first [Request].
	 *
	 * @property relativeTime the relative execution time (in ns) at which `actor` is to be scheduled
	 * @param actor the [Actor] for which the first [Request] is added to this [Slot]
	 * @param data the [ActorData] of the first [Request] to be added to this [Slot]
	 */
	private inner class Slot(
		val relativeTime: Long,
		actor: Actor,
		data: ActorData
	) : Comparable<Slot> {

		val isExecutable: Boolean get() = requests.values.any { it.isActable }

		val isActing: Boolean get() = requests.values.any { it.isActing }

		val empty: Boolean get() = requests.isEmpty()

		/**
		 * Contains the [Actor]s to the scheduled at the specified [relativeTime]. A particular [Actor]
		 * can only be contained at most once.
		 */
		private val requests = mutableMapOf<Actor, Request>()


		init {
			addRequest(Request(actor, data))
		}

		override fun compareTo(other: Slot): Int = relativeTime.compareTo(other.relativeTime)

		fun getRequests(): Iterable<Request> = requests.values

		/**
		 * Adds the specified [Actor] and its [ActorData] as a new [Request] to this [Slot].
		 * If the [Actor] is already present, its [ActorData] is updated.
		 * */
		fun addActor(actor: Actor, data: ActorData) {
			val request = findRequest(actor)
			if (request != null) {
				request.reuse(data)
			} else {
				addRequest(Request(actor, data))
			}
		}

		fun actingDone(actor: Actor, request: Request?) {
			request?.let {
				logActorTrace(actor) { "Actor is done " }
				requests.remove(it.actor)
				postSchedulerEvent(actor, SchedulerEvent.Type.DONE)
			}
		}

		fun findRequest(actor: Actor): Request? = requests[actor]

		/** Prints this [Slot] to the DEBUG log.*/
		fun print() {
			LOG.debug("\tSlot at ${StringUtils.formatLong(relativeTime)} ns with ${requests.size} requests")
			requests.values.forEach { it.print() }
		}

		private fun addRequest(request: Request) {
			logActorTrace(request.actor) { "Add actor to slot ${StringUtils.formatLong(relativeTime)} ns" }
			requests[request.actor] = request
		}
	}

	private inner class Request(
		val actor: Actor,
		actorData: ActorData
	) {

		private var _isActing: Boolean = false

		var actorData: ActorData = actorData
			private set

		/** `true` if [actor] has already been asked to execute.*/
		val isActing: Boolean get() = _isActing

		val isActable: Boolean get() = !isActing

		fun reuse(actorData: ActorData) {
			this.actorData = actorData
			_isActing = false
		}

		fun act() {
			if (!_isActing) {
				logActorTrace(actor) { "Actor starts acting" }
				_isActing = true
				actor.act(this@SchedulerImpl, actorData)
			}
		}

		/** Prints this [Request] to the INFO log.*/
		fun print() {
			LOG.info("\t\tRequest for ${actor::class.simpleName} with ID ${actor.id}, isActing=$_isActing")
		}
	}
}