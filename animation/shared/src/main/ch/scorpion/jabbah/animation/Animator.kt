package ch.scorpion.jabbah.animation

/**
 * An [Animator] is the central orchestration point of [AnimationTask] animations.
 */
interface Animator {

    /** Holds the number of scheduled [AnimationTask]s of this [Animator].*/
    val taskCount: Int

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

    /** Stops all currently running [AnimationTask]s.*/
    fun stopAllTasks()

}