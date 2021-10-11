package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.time.SystemSpeed

/**
 * An [AnimationTask] is the executable unit of the animation framework.
 */
interface AnimationTask {

    /**
     * Holds the object that is being animated.
     * Only used for retrieving [AnimationTask]s for a particular animated object from the [Animator].
     */
    val target: Any

    /** Holds the duration of time (in milliseconds) that this [AnimationTask] lasts.*/
    val duration: Double

    /**
     * Holds the overall size of this [AnimationTask]. The interpretation of "size" depends on the concrete
     * [AnimationTask] and the [Sequence] it used. For example, when object in the (x,y) plane are animated,
     * the size corresponds to the distance between the start and end points.*/
    val size: Double

    /**
     * Determines whether the speed of this [AnimationTask] depends on the current value of [SystemSpeed].
     */
    val dependsOnSystemSpeed: Boolean

    /** An optional identifying key allowing for example to retrieve all running [AnimationTask]s of the same type.*/
    val key: String?

    /** Determines whether this [AnimationTask] can be paused. */
    val isPausable: Boolean

    /** Adds [listener] to be informed about life cycle changed of this [AnimationTask].*/
    fun addListener(listener: AnimationTaskListener): AnimationTask

    /** Removes [listener] to stop being informed about life cycle changed of this [AnimationTask].*/
    fun removeListener(listener: AnimationTaskListener)

    /** Starts the animation that is represented by this [AnimationTask].*/
    fun start()

    /** Stops the animation that is represented by this [AnimationTask].*/
    fun stop()

    /** Performs a single animation step by pushing the animation value further by the specifed distance.*/
    fun animate(distance: Double)

    /**
     * Notifies this [AnimationTask] that it has been scheduled for execution to an [Animator].
     * Typically called by the [Animator].*/
    fun scheduled()
}

typealias AnimationTaskConsumer<T> = (T) -> Unit