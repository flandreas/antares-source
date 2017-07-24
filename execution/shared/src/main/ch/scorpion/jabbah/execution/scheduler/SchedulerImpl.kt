package ch.scorpion.jabbah.execution.scheduler

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
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger
import kotlin.reflect.KClass

/**
 * Standard implementation of the [Scheduler] interface.
 */
class SchedulerImpl(
    private val timeService: TimeService,
    timer: Timer,
    private val eventBus: EventBus,
    private val noiseGeneratorHolder: NoiseGeneratorHolder,
    interval: Int = 1
) : Scheduler {

    constructor(): this(
        BaseModule.timeService,
        System.get().createTimer(),
        BaseModule.eventBus,
        ExecutionModule.noiseGeneratorHolder)

    companion object {
        private val PROP_EXECUTION_DEPTH = "execution.scheduler.deepExecution"
    }

    private val LOG by logger(SchedulerImpl::class)

    /** The queue of pending [Slot]s ordered by ascending execution time.*/
    private val queue = PriorityQueue<Slot>()

    /** The object that is repeatedly called by the specified [Timer] in order to perform execution steps.*/
    private val task = Task(timer, interval)

    /** Determines whether this [Scheduler] is active or not.*/
    private var activationState: SchedulerActivationState = SchedulerActivationState.PASSIVE

    /** Determines whether this [Scheduler] is currently running or paused (single step mode).*/
    private var runningState: SchedulerRunningState = SchedulerRunningState.RUNNING

    /** The relative time [in ns] since execution has been started. */
    private var relativeTime: Long = 0

    /** Holds the absolute real time in nanoseconds of when the execution has been started.*/
    private var realStartTime: Long = 0

    /** ---- [Scheduler] interface */

    override val signalHandler: SignalHandler
        get() = this

    override val numberOfRemainingSlots: Int
        get() = queue.size

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
            eventBus.post(SchedulerActivationStateEvent(this))
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

    override fun step() {
        if (!isActive) {
            throw IllegalStateException("cannot step when not active")
        }
        if (!isPaused) {
            throw IllegalStateException("cannot step when not paused")
        }
        do {
            val result = task.executionStep()
        } while (result.recalculated && !result.breakpoint)
    }

    override fun proceedTo(time: Long) {
        while (!queue.isEmpty && executionTime < time) {
            task.executionStep()
        }
    }

    /** ---- [SignalHandler] interface */

    override var isDeepExecution: Boolean = BaseModule.properties.getString(PROP_EXECUTION_DEPTH, "true") == "true"
        set(value) {
            if (field == value) {
                return
            }
            field = value
            BaseModule.properties.set(PROP_EXECUTION_DEPTH, field)
            eventBus.post(ExecutionDepthEvent(this, field))
        }

    override val executionTime: Long
        get() = relativeTime

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
        logTrace(System.get().getClass(actor), actor.id, {"Acting done"})
        val slot = queue.peek()
        if (slot != null && slot.isExecuted) {
            val request = slot.findRequest(actor)
            if (request != null) {
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
        logTrace(System.get().getClass(actor), actor.id, {"Request to act after $delay ns"})
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
        }
        postSchedulerEvent(actor, SchedulerEvent.Type.REQUESTED)
    }

    /** ---- [SchedulerImpl] */

    private fun postSchedulerEvent(actor: Actor, type: SchedulerEvent.Type) {
        // TODO The ScenarioDetector is currently the only consumer of SimulationEvents. ScenarioDetector
        // is only active when the simulation is in stepping mode. For performance reasons, we therefore avoid sending
        // unnecessary (and costly) events.
        // Enable scenario detection only for slow simulations, e.g. when stepping through the simulation
        if (isActive && isPaused) {
            eventBus.post(SchedulerEvent(type, this, actor))
        }
    }

    private fun postSchedulerStateEvent() {
        eventBus.post(SchedulerStateEvent(numberOfRemainingSlots = queue.size, relativeTime = relativeTime))
    }

    private fun addSlot(slot: Slot) {
        LOG.trace("Add slot at ${slot.relativeTime}")
        queue.add(slot)
        if (!isPaused) {
            task.startIfNeeded()
        }
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

    private fun getRelativeRealTime(): Long {
        return timeService.nowNanos() - realStartTime
    }

    private fun getNextExecutableSlot(): Slot? {
        val slot = queue.peek()
        if (slot == null || !slot.isExecutable) {
            return null
        }
        return slot
    }

    /**
     * Repeatedly called by the [Timer] in order to perform an execution step.
     * @param interval the interval (in ms) at which the [Timer] calls this [Task]
     */
    private inner class Task(
        private val timer: Timer,
        interval: Int
    ) : ActionListener {

        init {
            timer.initialize(interval, {actionPerformed(it)})
        }

        override fun actionPerformed(event: ActionEvent) {
            val beginTime = System.get().currentTimeMillis()

//            var count = 0
//            while (count < 100 && executionStep().recalculated) {
//                count++
//            }

            while (System.get().currentTimeMillis() - beginTime < 20) {
                executionStep()
            }
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

        fun executionStep(): ExecutionStepResult {
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
                slot.isExecuted = true
                for (request in slot.getRequests()) {
                    logTrace(System.get().getClass(request.actor), request.actor.id, {"Executing"})
                    breakpoint = breakpoint || request.actor.isBreakpoint
                    if (request.actor.act(this@SchedulerImpl, request.actorData)) {
                        request.setDone()
                    }
                }
                if (slot.isDone) {
                    removeSlot(slot)
                }
                recalculated = true
            } else {
                updateRelativeTime(Math.min(getRelativeRealTime(), slot.relativeTime))
            }
            postSchedulerStateEvent()
            return ExecutionStepResult(recalculated, breakpoint)
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
     */
    private inner class Slot(
            val relativeTime: Long,
            var timeFreeze: Boolean,
            actor: Actor,
            data: ActorData
    ) : Comparable<Slot> {

        var isExecuted: Boolean = false

        val isExecutable: Boolean get() = !isExecuted

        val isDone: Boolean get() = requests.all { it.done }

        /**
         * Contains the [Actor]s to the scheduled at the specified [relativeTime]. A particular [Actor]
         * can only be contained at most once.
         */
        private val requests = mutableListOf<Request>()


        init {
            requests.add(Request(actor, data))
        }

        override fun compareTo(other: Slot): Int {
            return relativeTime.compareTo(other.relativeTime)
        }

        fun getRequests(): Iterable<Request> {
            return requests
        }

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

        fun findRequest(actor: Actor): Request? {
            return requests.find { it.actor === actor }
        }
    }

    private inner class Request(
        val actor: Actor,
        var actorData: ActorData
    ) {

        private var _done: Boolean = false

        val done: Boolean
            get() = _done

        fun setDone() {
            if (_done) {
                return
            }
            _done = true
            postSchedulerEvent(actor, SchedulerEvent.Type.DONE)
        }
    }
}