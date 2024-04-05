package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.math.SIGMA
import ch.scorpion.jabbah.base.time.SystemSpeed
import kotlin.sequences.Sequence

/**
 * Enhances a scheduled [AnimationTask]s with runtime information.
 * @property task the [AnimationTask] being managed by this [AnimationJob].
 * @property maxDistance the maximum distance of [Sequence] values to be used if [task] doesn't
 * depend on [SystemSpeed]. If it does, the effectively used distance is shortened according to the
 * [SystemSpeed]'s current value.
 */
class AnimationJob(
	val task: AnimationTask,
	private val maxDistance: Double,
	private val systemSpeed: SystemSpeed
) {

	companion object {
		private val LOG by logger(AnimationJob::class)
	}

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
		currentDistance().let {
			if (it < SIGMA) {
				suspend()
			} else {
				task.animate(it)
			}
		}
	}

	/** Calculates the distance to be used for the current animation step. Can depend on [SystemSpeed].*/
	private fun currentDistance(): Double {
		if (!task.dependsOnSystemSpeed) {
			return maxDistance
		}
		return systemSpeed.speed / 100.0 * maxDistance
	}
}