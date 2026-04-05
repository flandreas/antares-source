package io.antarescircuit.jabbah.animation

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.Timer
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.base.time.SystemSpeedEvent
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.time.SystemSpeedPauseEvent

/**
 * A standard implementation of an [io.antarescircuit.jabbah.animation.Animator].
 *
 * @param timer the [io.antarescircuit.jabbah.base.time.Timer] that delivers the animation pulses
 * @param period the time in milliseconds between two animation pulses
 */
class AnimatorImpl(
    override val systemSpeed: io.antarescircuit.jabbah.base.time.SystemSpeed,
    private val timer: io.antarescircuit.jabbah.base.time.Timer = _root_ide_package_.io.antarescircuit.jabbah.base.System.createTimer(),
    private val period: Int = DEFAULT_PERIOD,
    private val eventBus: io.antarescircuit.jabbah.base.event.EventBus = _root_ide_package_.io.antarescircuit.jabbah.base.module.BaseModule.eventBus,
) : io.antarescircuit.jabbah.animation.Animator {

	private companion object {
		val LOG by _root_ide_package_.io.antarescircuit.jabbah.base.logger(AnimatorImpl::class)

		// The default animation pulse in milliseconds
		const val DEFAULT_PERIOD = 20
	}

	/** Listens for started and isEnded [io.antarescircuit.jabbah.animation.AnimationTask] of this [AnimatorImpl]. Visible for testing.*/
	val taskListener: io.antarescircuit.jabbah.animation.AnimationTaskListener = TaskListener()

	/** Holds all scheduled [io.antarescircuit.jabbah.animation.AnimationTask]s.*/
	private val jobs: MutableList<io.antarescircuit.jabbah.animation.AnimationJob> by lazy { mutableListOf() }

	private val systemSpeedHandler: io.antarescircuit.jabbah.base.event.EventHandler<io.antarescircuit.jabbah.base.time.SystemSpeedEvent> = { handle(it) }

	private val pauseEventListener: io.antarescircuit.jabbah.base.event.EventHandler<io.antarescircuit.jabbah.base.time.SystemSpeedPauseEvent> = { handle(it) }

	init {
		timer.initialize(period) { animationStep() }
		eventBus.register(_root_ide_package_.io.antarescircuit.jabbah.base.time.SystemSpeedEvent::class, systemSpeedHandler)
		eventBus.register(_root_ide_package_.io.antarescircuit.jabbah.base.time.SystemSpeedPauseEvent::class, pauseEventListener)
	}

	override fun dispose() {
		eventBus.unregister(systemSpeedHandler)
		eventBus.unregister(pauseEventListener)
	}

	/** ---- [io.antarescircuit.jabbah.animation.Animator] interface */

	override val taskCount: Int get() = jobs.size

	override fun schedule(task: io.antarescircuit.jabbah.animation.AnimationTask): io.antarescircuit.jabbah.animation.AnimationTask {
		LOG.trace("Scheduling AnimationTask $task")
		task.addListener(taskListener)
		jobs.add(
            _root_ide_package_.io.antarescircuit.jabbah.animation.AnimationJob(
                task,
                calculateDistance(task),
                systemSpeed
            )
        )
		task.scheduled()
		return task
	}

	override fun <T> schedule(
        target: Any,
        consumer: io.antarescircuit.jabbah.animation.AnimationTaskConsumer<T>,
        sequence: io.antarescircuit.jabbah.animation.Sequence<T>,
        duration: Double,
        dependsOnSystemSpeed: Boolean
	): io.antarescircuit.jabbah.animation.AnimationTask = schedule(
        _root_ide_package_.io.antarescircuit.jabbah.animation.AnimationTaskImpl(
            target,
            consumer,
            sequence,
            duration,
            dependsOnSystemSpeed
        )
    )

	override fun getTasksForTarget(target: Any): Collection<io.antarescircuit.jabbah.animation.AnimationTask> {
		val tasks = mutableSetOf<io.antarescircuit.jabbah.animation.AnimationTask>()
		val jobList = jobs.toList()
		jobList
			.filter { it.task.target == target }
			.forEach { tasks.add(it.task) }

		return tasks
	}

	override fun getTasksForKey(key: String): Collection<io.antarescircuit.jabbah.animation.AnimationTask> =
		jobs.filter { it.task.key == key }.map { it.task }

	override fun stopAllTasks() {
		jobs.toList().forEach { it.task.stop() }
	}

	override fun cancelAllTasks() {
		jobs.toList().forEach { it.task.cancel() }
	}

	/** ---- [AnimatorImpl] */

	private fun handle(event: io.antarescircuit.jabbah.base.time.SystemSpeedPauseEvent) {
		if (event.source === systemSpeed) {
			// TODO: Suspend Task if all running Jobs are pausable
		}
	}

	private fun handle(event: io.antarescircuit.jabbah.base.time.SystemSpeedEvent) {
		if (event.source === systemSpeed) {
			if (event.oldSpeed == 0 && event.newSpeed > 0) {
				resumeSuspendedJobs()
			}
		}
	}

	/** Calculates the distance between two steps of an [io.antarescircuit.jabbah.animation.AnimationTask].*/
	private fun calculateDistance(task: io.antarescircuit.jabbah.animation.AnimationTask): Double =
		task.size / (task.duration / period)

	/** Finds the [io.antarescircuit.jabbah.animation.AnimationJob] for the specific [io.antarescircuit.jabbah.animation.AnimationTask].*/
	private fun findJob(task: io.antarescircuit.jabbah.animation.AnimationTask): io.antarescircuit.jabbah.animation.AnimationJob =
		jobs.first { it.task === task }

	/**
	 * Periodically called by the animation timer to perform a single animation step by proceeding every
	 * isRunning [io.antarescircuit.jabbah.animation.AnimationTask] on step further.
	 */
	private fun animationStep() {
		jobs.toList()
			.filter { it.isRunning && (!systemSpeed.isPaused || !it.task.isPausable) }
			.forEach { it.animate() }

		removeEndedJobs()
	}

	private fun removeEndedJobs() {
		jobs.removeAll { it.isEnded }
		if (!hasRunningJobs()) {
			timer.stop()
		}
	}

	private fun hasRunningJobs(): Boolean = jobs.any { it.isRunning }

	private fun resumeSuspendedJobs() {
		LOG.trace("resume suspended Jobs")
		jobs.filter { it.isSuspended }.forEach { it.resume() }
		startTimerIfNeeded()
	}

	private fun startTimerIfNeeded() {
		if (!timer.isRunning() && hasRunningJobs()) {
			timer.start()
		}
	}

	/** Listens for started and isEnded [io.antarescircuit.jabbah.animation.AnimationTask] of this [AnimatorImpl].*/
	private inner class TaskListener : io.antarescircuit.jabbah.animation.AnimationTaskAdapter() {

		override fun started(task: io.antarescircuit.jabbah.animation.AnimationTask) {
			LOG.trace("$task started")
			findJob(task).start()
			startTimerIfNeeded()
		}

		override fun ended(task: io.antarescircuit.jabbah.animation.AnimationTask, canceled: Boolean) {
			findJob(task).end()
			task.removeListener(taskListener)
			removeEndedJobs()
			LOG.trace("$task ended, $taskCount remaining tasks")
		}
	}
}