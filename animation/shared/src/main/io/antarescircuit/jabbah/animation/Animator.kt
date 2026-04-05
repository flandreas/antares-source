package io.antarescircuit.jabbah.animation

import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.base.time.SystemSpeedPauseEvent

/**
 * An [Animator] is the central orchestration point of [io.antarescircuit.jabbah.animation.AnimationTask] animations.
 * Listens for [io.antarescircuit.jabbah.base.time.SystemSpeedPauseEvent]s from the [io.antarescircuit.jabbah.base.time.SystemSpeed] to pause and resume this [Animator].
 */
interface Animator {

	/** The [io.antarescircuit.jabbah.base.time.SystemSpeed] influencing the execution speed of [io.antarescircuit.jabbah.animation.AnimationTask]s animated by this [Animator].*/
	val systemSpeed: io.antarescircuit.jabbah.base.time.SystemSpeed

    /** Holds the number of scheduled [io.antarescircuit.jabbah.animation.AnimationTask]s of this [Animator].*/
    val taskCount: Int

    fun dispose()

    /**
     * Adds [task] to prepare of its animation.
     * Note that this doesn't start the animation yet. Starting scheduled animations is accomplished by
     * [io.antarescircuit.jabbah.animation.AnimationTask.start].
     * @return the argument [task] to support method chaining
     */
    fun schedule(task: io.antarescircuit.jabbah.animation.AnimationTask): io.antarescircuit.jabbah.animation.AnimationTask

    fun <T> schedule(
        target: Any,
        consumer: io.antarescircuit.jabbah.animation.AnimationTaskConsumer<T>,
        sequence: io.antarescircuit.jabbah.animation.Sequence<T>,
        duration: Double,
        dependsOnSystemSpeed: Boolean = false
    ): io.antarescircuit.jabbah.animation.AnimationTask

    /** Returns all scheduled [io.antarescircuit.jabbah.animation.AnimationTask]s for the specified [target].*/
    fun getTasksForTarget(target: Any): Collection<io.antarescircuit.jabbah.animation.AnimationTask>

    /** Returns all scheduled [io.antarescircuit.jabbah.animation.AnimationTask]s with the specified key.*/
    fun getTasksForKey(key: String): Collection<io.antarescircuit.jabbah.animation.AnimationTask>

    /** Stops all currently running [io.antarescircuit.jabbah.animation.AnimationTask]s.*/
    fun stopAllTasks()

    fun cancelAllTasks()
}