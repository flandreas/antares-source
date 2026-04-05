package io.antarescircuit.jabbah.animation

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.math.SIGMA
import io.antarescircuit.jabbah.base.time.SystemSpeed
import kotlin.sequences.Sequence

/**
 * Enhances a scheduled [io.antarescircuit.jabbah.animation.AnimationTask]s with runtime information.
 * @property task the [io.antarescircuit.jabbah.animation.AnimationTask] being managed by this [AnimationJob].
 * @property maxDistance the maximum distance of [Sequence] values to be used if [task] doesn't
 * depend on [io.antarescircuit.jabbah.base.time.SystemSpeed]. If it does, the effectively used distance is shortened according to the
 * [io.antarescircuit.jabbah.base.time.SystemSpeed]'s current value.
 */
class AnimationJob(
    val task: io.antarescircuit.jabbah.animation.AnimationTask,
    private val maxDistance: Double,
    private val systemSpeed: io.antarescircuit.jabbah.base.time.SystemSpeed
) {

	companion object {
		private val LOG by _root_ide_package_.io.antarescircuit.jabbah.base.logger(AnimationJob::class)
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
			if (it < _root_ide_package_.io.antarescircuit.jabbah.base.math.SIGMA) {
				suspend()
			} else {
				task.animate(it)
			}
		}
	}

	/** Calculates the distance to be used for the current animation step. Can depend on [io.antarescircuit.jabbah.base.time.SystemSpeed].*/
	private fun currentDistance(): Double {
		if (!task.dependsOnSystemSpeed) {
			return maxDistance
		}
		return systemSpeed.speed / 100.0 * maxDistance
	}
}