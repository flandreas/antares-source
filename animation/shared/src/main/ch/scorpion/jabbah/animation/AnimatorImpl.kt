package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.System
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
	private val systemSpeed: SystemSpeed,
	private val timer: Timer = System.createTimer(),
	private val period: Int = DEFAULT_PERIOD,
	eventBus: EventBus = BaseModule.eventBus,
) : Animator {

	init {
		timer.initialize(period) { animationStep() }
		eventBus.register(SystemSpeedEvent::class) {
			if (it.oldSpeed == 0 && it.newSpeed > 0) {
				resumeSuspendedJobs()
			}
		}
	}

	private companion object {
		val LOG by logger(AnimatorImpl::class)
		// The default animation pulse in milliseconds
		const val DEFAULT_PERIOD = 20
	}


	/** Listens for started and isEnded [AnimationTask] of this [AnimatorImpl]. Visible for testing.*/
	val taskListener: AnimationTaskListener = TaskListener()

	/** Holds all scheduled [AnimationTask]s.*/
	private val jobs: MutableList<AnimationJob> by lazy { mutableListOf() }

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
	): AnimationTask {
		return schedule(AnimationTaskImpl(target, consumer, sequence, duration, dependsOnSystemSpeed))
	}

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

	/** Calculates the distance between two steps of an [AnimationTask].*/
	private fun calculateDistance(task: AnimationTask): Double {
		return task.size / (task.duration / period)
	}

	/** Finds the [AnimationJob] for the specific [AnimationTask].*/
	private fun findJob(task: AnimationTask): AnimationJob {
		return jobs.first { it.task === task }
	}

	/**
	 * Periodically called by the animation timer to perform a single animation step by proceeding every
	 * isRunning [AnimationTask] on step further.
	 */
	private fun animationStep() {
		LOG.trace("animationStep")
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
		return jobs.any { it.isRunning }
	}

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
			LOG.trace("suspending job")
			state = State.Suspended
		}

		fun end() {
			state = State.Ended
		}

		fun resume() {
			LOG.trace("resuming job")
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
			LOG.trace("$task started, $taskCount remaining tasks")
		}
	}
}