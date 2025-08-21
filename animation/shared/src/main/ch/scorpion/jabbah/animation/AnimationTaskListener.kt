package ch.scorpion.jabbah.animation

/**
 * Listens for important lifecycle state changes of an [AnimationTask].
 */
interface AnimationTaskListener {

    /** Notifies this [AnimationTaskListener] that [task] has been scheduled for execution in an [Animator].*/
    fun scheduled(task: AnimationTask)

    /** Notifies this [AnimationTaskListener] that [task] has been started in an [Animator].*/
    fun started(task: AnimationTask)

    /** Notifies this [AnimationTaskListener] that [task] has been ended in an [Animator].*/
    fun ended(task: AnimationTask, canceled: Boolean = false)
}

/**
 * Empty implementation of [AnimationTaskListener] intended to be subclassed by listeners that
 * only need to react to some of the lifecycle events.
 */
open class AnimationTaskAdapter : AnimationTaskListener {

    override fun scheduled(task: AnimationTask) {}

    override fun started(task: AnimationTask) {}

    override fun ended(task: AnimationTask, canceled: Boolean) {}
}