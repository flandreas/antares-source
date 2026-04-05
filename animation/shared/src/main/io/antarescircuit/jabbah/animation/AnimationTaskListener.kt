package io.antarescircuit.jabbah.animation

/**
 * Listens for important lifecycle state changes of an [io.antarescircuit.jabbah.animation.AnimationTask].
 */
interface AnimationTaskListener {

    /** Notifies this [AnimationTaskListener] that [task] has been scheduled for execution in an [io.antarescircuit.jabbah.animation.Animator].*/
    fun scheduled(task: io.antarescircuit.jabbah.animation.AnimationTask)

    /** Notifies this [AnimationTaskListener] that [task] has been started in an [io.antarescircuit.jabbah.animation.Animator].*/
    fun started(task: io.antarescircuit.jabbah.animation.AnimationTask)

    /** Notifies this [AnimationTaskListener] that [task] has been ended in an [io.antarescircuit.jabbah.animation.Animator].*/
    fun ended(task: io.antarescircuit.jabbah.animation.AnimationTask, canceled: Boolean = false)
}

/**
 * Empty implementation of [io.antarescircuit.jabbah.animation.AnimationTaskListener] intended to be subclassed by listeners that
 * only need to react to some of the lifecycle events.
 */
open class AnimationTaskAdapter : io.antarescircuit.jabbah.animation.AnimationTaskListener {

    override fun scheduled(task: io.antarescircuit.jabbah.animation.AnimationTask) {}

    override fun started(task: io.antarescircuit.jabbah.animation.AnimationTask) {}

    override fun ended(task: io.antarescircuit.jabbah.animation.AnimationTask, canceled: Boolean) {}
}