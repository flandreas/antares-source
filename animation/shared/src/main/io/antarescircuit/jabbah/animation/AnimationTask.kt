package io.antarescircuit.jabbah.animation

import io.antarescircuit.jabbah.base.time.SystemSpeed

/**
 * An [AnimationTask] is the executable unit of the animation framework.
 */
interface AnimationTask {

    /**
     * Holds the object that is being animated.
     * Only used for retrieving [AnimationTask]s for a particular animated object from the [io.antarescircuit.jabbah.animation.Animator].
     */
    val target: Any

    /** Holds the duration of time (in milliseconds) that this [AnimationTask] lasts.*/
    val duration: Double

    /**
     * Holds the overall size of this [AnimationTask]. The interpretation of "size" depends on the concrete
     * [AnimationTask] and the [io.antarescircuit.jabbah.animation.Sequence] it used. For example, when object in the (x,y) plane are animated,
     * the size corresponds to the distance between the start and end points.*/
    val size: Double

    /**
     * Determines whether the speed of this [AnimationTask] depends on the current value of [io.antarescircuit.jabbah.base.time.SystemSpeed].
     */
    val dependsOnSystemSpeed: Boolean

    /** An optional identifying key allowing for example to retrieve all running [AnimationTask]s of the same type.*/
    val key: String?

    /** Determines whether this [AnimationTask] can be paused. */
    val isPausable: Boolean

    /** Adds [listener] to be informed about life cycle changed of this [AnimationTask].*/
    fun addListener(listener: io.antarescircuit.jabbah.animation.AnimationTaskListener): AnimationTask

    /** Removes [listener] to stop being informed about life cycle changed of this [AnimationTask].*/
    fun removeListener(listener: io.antarescircuit.jabbah.animation.AnimationTaskListener)

    /**
     * Starts the animation that is represented by this [AnimationTask].
     * Presumes that this [AnimationTask] has been scheduled with an [io.antarescircuit.jabbah.animation.Animator] before.
     * Registered [io.antarescircuit.jabbah.animation.AnimationTaskListener] are notified with [io.antarescircuit.jabbah.animation.AnimationTaskListener.started].
     */
    fun start()

    /**
     * Stops the animation that is represented by this [AnimationTask].
     * Called when an [AnimationTask] has regularly completed its job, and other [AnimationTasks][AnimationTask]
     * might be started as a follow-up.
     * Registered [io.antarescircuit.jabbah.animation.AnimationTaskListener] are notified with [io.antarescircuit.jabbah.animation.AnimationTaskListener.ended].
     */
    fun stop()

    /**
     * Cancels the animation represented by this [AnimationTask].
     * Called when animation has been interrupted by the user. In contrast to [stop], other [AnimationTasks][AnimationTask]
     * depending on this one are NOT started as a follow-up.
     */
    fun cancel()

    /** Performs a single animation step by pushing the animation value further by the specified distance.*/
    fun animate(distance: Double)

    /**
     * Notifies this [AnimationTask] that it has been scheduled for execution to an [io.antarescircuit.jabbah.animation.Animator].
     * Typically called by the [io.antarescircuit.jabbah.animation.Animator].*/
    fun scheduled()
}

typealias AnimationTaskConsumer<T> = (T) -> Unit