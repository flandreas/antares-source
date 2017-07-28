package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.SystemSpeedEvent
import ch.scorpion.jabbah.base.logger

/**
 * A standard implementation of an [Animator].
 *
 * @param timer the [Timer] that delivers the animation pulses
 * @param period the time in milliseconds between two animation pulses
 */
class AnimatorImpl(
        private val timer: Timer,
        private val period: Int = AnimatorImpl.DEFAULT_PERIOD,
        eventBus: EventBus = BaseModule.eventBus,
        private val systemSpeed: SystemSpeed = BaseModule.systemSpeed
) : Animator {

    init {
        timer.initialize(period, { animationStep()} )
        eventBus.register(SystemSpeedEvent::class, {
            if (it.oldSpeed == 0 && it.newSpeed > 0) {
                resumeSuspendedJobs()
            }
        })
    }

    private companion object {
        val LOG by logger(AnimatorImpl::class)
        // The default animation pulse in milliseconds
        val DEFAULT_PERIOD = 20
    }



    /** Listens for started and isEnded [AnimationTask] of this [AnimatorImpl]. Visible for testing.*/
    val taskListener: AnimationTaskListener = TaskListener()

    /** Holds all scheduled [AnimationTask]s.*/
    private val jobs: MutableList<AnimationJob> by lazy { mutableListOf<AnimationJob>() }

    /** ---- [Animator] interface */

    override val taskCount: Int get() = jobs.size

    override fun schedule(task: AnimationTask): AnimationTask {
        LOG.debug("Scheduling AnimationTask $task")
        task.addListener(taskListener)
        jobs.add(AnimationJob(task, calculateDistance(task), systemSpeed))
        task.scheduled()
        return task
    }

    override fun <T> schedule(
        target: Any,
        consumer: AnimationTaskConsumer<T>,
        sequence: Sequence<T>,
        duration: Double,
        dependsOnSystemSpeed: Boolean
    ): AnimationTask {
        return schedule(AnimationTaskImpl(target, consumer, sequence, duration, dependsOnSystemSpeed))
    }

    override fun getTasksForTarget(target: Any): Collection<AnimationTask> {
        val tasks = mutableSetOf<AnimationTask>()
        val jobList = jobs.toList()
        jobList
            .filter { it.task.target == target}
            .forEach { tasks.add(it.task) }

        return tasks
    }

    override fun stopAllTasks() {
        jobs.toList().forEach { it.task.stop() }
    }

    /** ---- [AnimatorImpl] */

    /** Calculates the distance between two steps of an [AnimationTask].*/
    private fun calculateDistance(task: AnimationTask): Double {
        return task.size / (task.duration / period)
    }

    /** Finds the [AnimationJob] for the specific [AnimationTask].*/
    private fun findJob(task: AnimationTask): AnimationJob {
        return jobs.filter { it.task === task }.first()
    }

    /**
     * Periodically called by the animation timer to perform a single animation step by proceeding every
     * isRunning [AnimationTask] on step further.
     */
    private fun animationStep() {
        LOG.trace("AnimatorImpl: animationStep")
        jobs.toList()
            .filter { jobs.contains(it) && it.isRunning }
            .forEach { it.animate() }

        removeEndedJobs()
    }

    private fun removeEndedJobs() {
        val iter = jobs.iterator()
        while (iter.hasNext()) {
            if (iter.next().isEnded) {
                iter.remove()
            }
        }

        if (!hasRunningJobs()) {
            timer.stop()
        }
    }

    private fun hasRunningJobs(): Boolean {
        return jobs.filter { it.isRunning }.isNotEmpty()
    }

    private fun resumeSuspendedJobs() {
        LOG.debug("AnimatorImpl: resume suspended Jobs")
        jobs.filter { it.isSuspended }.forEach { it.resume() }
        startTimerIfNeeded()
    }

    private fun startTimerIfNeeded() {
        if (!timer.isRunning() && hasRunningJobs()) {
            timer.start()
        }
    }

    /**
     * Enhances a scheduled [AnimationTask]s with runtime information.
     * @property task the [AnimationTask] being managed by this [AnimationJob].
     * @property maxDistance the maximum distance of [Sequence] values to be used if [task] doesn't
     * depend on [SystemSpeed]. If it does, the effectively used distance is shortened according to the
     * [SystemSpeed]'s current value.
     */
    private class AnimationJob(val task: AnimationTask, private val maxDistance: Double, private val systemSpeed: SystemSpeed) {

        enum class State {
            Created,
            Running,
            Suspended,
            Ended
        }

        /** Holds the current [State] of this [AnimationJob]. */
        private var state: State = State.Created

        /** Determines whether [task] is currently isRunning.*/
        val isRunning: Boolean get() = state == State.Running

        val isSuspended: Boolean get() = state == State.Suspended

        /** Determines whether [task] has already isEnded.*/
        val isEnded: Boolean get() = state == State.Ended

        fun start() {
            state = State.Running
        }

        fun suspend() {
            LOG.debug("AnimatorImpl: suspending job")
            state = State.Suspended
        }

        fun end() {
            state = State.Ended
        }

        fun resume() {
            LOG.debug("AnimatorImpl: resuming job")
            state = State.Running
        }

        fun animate() {
            task.animate(currentDistance())
        }

        /** Calculates the distance to be used for the current animation step. Can depend on [SystemSpeed].*/
        private fun currentDistance(): Double {
            if (!task.dependsOnSystemSpeed) {
                return maxDistance
            }
            val distance = systemSpeed.speed / 100.0 * maxDistance
            if (distance == 0.0) {
                suspend()
            }
            return distance
        }
    }

    /** Listens for started and isEnded [AnimationTask] of this [AnimatorImpl].*/
    inner private class TaskListener : AnimationTaskAdapter() {

        override fun started(task: AnimationTask) {
            LOG.debug("$task started")
            findJob(task).start()
            startTimerIfNeeded()
        }

        override fun ended(task: AnimationTask) {
            findJob(task).end()
            task.removeListener(taskListener)
            removeEndedJobs()
            LOG.debug("$task started, $taskCount remaining tasks")
        }
    }
}