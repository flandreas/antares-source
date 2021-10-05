package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.SystemSpeedPauseEvent

/**
 * An [Animator] is the central orchestration point of [AnimationTask] animations.
 * Listens for [SystemSpeedPauseEvent]s from the [SystemSpeed] to pause and resume this [Animator].
 */
interface Animator {

	/** The [SystemSpeed] influencing the execution speed of [AnimationTask]s animated by this [Animator].*/
	val systemSpeed: SystemSpeed

    /** Holds the number of scheduled [AnimationTask]s of this [Animator].*/
    val taskCount: Int

    fun dispose()

    /**
     * Adds [task] to prepare of its animation.
     * Note that this doesn't start the animation yet. Starting scheduled animations is accomplished by
     * [AnimationTask.start].
     * @return the argument [task] to support method chaining
     */
    fun schedule(task: AnimationTask): AnimationTask

    fun <T> schedule(
        target: Any,
        consumer: AnimationTaskConsumer<T>,
        sequence: Sequence<T>,
        duration: Double,
        dependsOnSystemSpeed: Boolean = false
    ): AnimationTask

    /** Returns all scheduled [AnimationTask]s for the specified [target].*/
    fun getTasksForTarget(target: Any): Collection<AnimationTask>

    /** Returns all scheduled [AnimationTask]s with the specified key.*/
    fun getTasksForKey(key: String): Collection<AnimationTask>

    /** Stops all currently running [AnimationTask]s.*/
    fun stopAllTasks()
}