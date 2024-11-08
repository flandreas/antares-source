package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.SystemSpeedEvent
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.time.SystemSpeedPauseEvent

/**
 * A standard implementation of an [Animator].
 *
 * @param timer the [Timer] that delivers the animation pulses
 * @param period the time in milliseconds between two animation pulses
 */
class AnimatorImpl(
	override val systemSpeed: SystemSpeed,
	private val timer: Timer = System.createTimer(),
	private val period: Int = DEFAULT_PERIOD,
	private val eventBus: EventBus = BaseModule.eventBus,
) : Animator {

	private companion object {
		val LOG by logger(AnimatorImpl::class)

		// The default animation pulse in milliseconds
		const val DEFAULT_PERIOD = 20
	}

	/** Listens for started and isEnded [AnimationTask] of this [AnimatorImpl]. Visible for testing.*/
	val taskListener: AnimationTaskListener = TaskListener()

	/** Holds all scheduled [AnimationTask]s.*/
	private val jobs: MutableList<AnimationJob> by lazy { mutableListOf() }

	private val systemSpeedHandler: EventHandler<SystemSpeedEvent> = { handle(it) }

	private val pauseEventListener: EventHandler<SystemSpeedPauseEvent> = { handle(it) }

	init {
		timer.initialize(period) { animationStep() }
		eventBus.register(SystemSpeedEvent::class, systemSpeedHandler)
		eventBus.register(SystemSpeedPauseEvent::class, pauseEventListener)
	}

	override fun dispose() {
		eventBus.unregister(systemSpeedHandler)
		eventBus.unregister(pauseEventListener)
	}

	/** ---- [Animator] interface */

	override val taskCount: Int get() = jobs.size

	override fun schedule(task: AnimationTask): AnimationTask {
		LOG.trace("Scheduling AnimationTask $task")
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
	): AnimationTask = schedule(AnimationTaskImpl(target, consumer, sequence, duration, dependsOnSystemSpeed))

	override fun getTasksForTarget(target: Any): Collection<AnimationTask> {
		val tasks = mutableSetOf<AnimationTask>()
		val jobList = jobs.toList()
		jobList
			.filter { it.task.target == target }
			.forEach { tasks.add(it.task) }

		return tasks
	}

	override fun getTasksForKey(key: String): Collection<AnimationTask> =
		jobs.filter { it.task.key == key }.map { it.task }

	override fun stopAllTasks() {
		jobs.toList().forEach { it.task.stop() }
	}

	/** ---- [AnimatorImpl] */

	private fun handle(event: SystemSpeedPauseEvent) {
		if (event.source === systemSpeed) {
			// TODO: Suspend Task if all running Jobs are pausable
		}
	}

	private fun handle(event: SystemSpeedEvent) {
		if (event.source === systemSpeed) {
			if (event.oldSpeed == 0 && event.newSpeed > 0) {
				resumeSuspendedJobs()
			}
		}
	}

	/** Calculates the distance between two steps of an [AnimationTask].*/
	private fun calculateDistance(task: AnimationTask): Double =
		task.size / (task.duration / period)

	/** Finds the [AnimationJob] for the specific [AnimationTask].*/
	private fun findJob(task: AnimationTask): AnimationJob =
		jobs.first { it.task === task }

	/**
	 * Periodically called by the animation timer to perform a single animation step by proceeding every
	 * isRunning [AnimationTask] on step further.
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

	/** Listens for started and isEnded [AnimationTask] of this [AnimatorImpl].*/
	private inner class TaskListener : AnimationTaskAdapter() {

		override fun started(task: AnimationTask) {
			LOG.trace("$task started")
			findJob(task).start()
			startTimerIfNeeded()
		}

		override fun ended(task: AnimationTask) {
			findJob(task).end()
			task.removeListener(taskListener)
			removeEndedJobs()
			LOG.trace("$task ended, $taskCount remaining tasks")
		}
	}
}