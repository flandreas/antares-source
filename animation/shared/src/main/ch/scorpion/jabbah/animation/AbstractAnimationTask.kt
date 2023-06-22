package ch.scorpion.jabbah.animation

/**
 * Base implementation of [AnimationTask] to be used for implementing custom types of [AnimationTask]s.
 * @param T the type of value that is animated
 */
abstract class AbstractAnimationTask<T>(
    override val target: Any,
    private val consumer: AnimationTaskConsumer<T>,
    private val sequence: Sequence<T>,
    override val duration: Double,
    override val dependsOnSystemSpeed: Boolean = false,
    override val isPausable: Boolean = false,
    override val key: String? = null
) : AnimationTask {

    private val listeners: MutableList<AnimationTaskListener> by lazy { mutableListOf() }

    /** ---- [AnimationTask] interface */

    override val size: Double
        get() = sequence.size

    override fun addListener(listener: AnimationTaskListener): AnimationTask {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
	    return this
    }

    override fun removeListener(listener: AnimationTaskListener) {
        listeners.remove(listener)
    }

    override fun start() {
	    handleStarted()
        listeners.forEach { it.started(this) }
    }

    override fun stop() {
	    handleStopped()
	    listeners.toList().forEach { it.ended(this) }
    }

    override fun animate(distance: Double) {
		sequence.getNext(distance)?.let { consumer.invoke(it) } ?: stop()
    }

    override fun scheduled() {
        listeners.forEach { it.scheduled(this) }
    }

    /** ---- [AbstractAnimationTask] */

    protected open fun handleStarted() {}

	protected open fun handleStopped() {}
}

class AnimationTaskImpl<T>(
    target: Any,
    consumer: AnimationTaskConsumer<T>,
    sequence: Sequence<T>,
    duration: Double,
    dependsOnSystemSpeed: Boolean = false
) : AbstractAnimationTask<T>(target, consumer, sequence, duration, dependsOnSystemSpeed)
