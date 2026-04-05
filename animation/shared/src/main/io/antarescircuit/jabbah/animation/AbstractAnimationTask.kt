package io.antarescircuit.jabbah.animation

/**
 * Base implementation of [io.antarescircuit.jabbah.animation.AnimationTask] to be used for implementing custom types of [io.antarescircuit.jabbah.animation.AnimationTask]s.
 * @param T the type of value that is animated
 */
abstract class AbstractAnimationTask<T>(
    override val target: Any,
    private val consumer: io.antarescircuit.jabbah.animation.AnimationTaskConsumer<T>,
    private val sequence: io.antarescircuit.jabbah.animation.Sequence<T>,
    override val duration: Double,
    override val dependsOnSystemSpeed: Boolean = false,
    override val isPausable: Boolean = false,
    override val key: String? = null
) : io.antarescircuit.jabbah.animation.AnimationTask {

    private val listeners: MutableList<io.antarescircuit.jabbah.animation.AnimationTaskListener> by lazy { mutableListOf() }

    /** ---- [io.antarescircuit.jabbah.animation.AnimationTask] interface */

    override val size: Double
        get() = sequence.size

    override fun addListener(listener: io.antarescircuit.jabbah.animation.AnimationTaskListener): io.antarescircuit.jabbah.animation.AnimationTask {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
	    return this
    }

    override fun removeListener(listener: io.antarescircuit.jabbah.animation.AnimationTaskListener) {
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

    override fun cancel() {
        handleStopped()
        listeners.toList().forEach { it.ended(this, canceled = true) }
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
    consumer: io.antarescircuit.jabbah.animation.AnimationTaskConsumer<T>,
    sequence: io.antarescircuit.jabbah.animation.Sequence<T>,
    duration: Double,
    dependsOnSystemSpeed: Boolean = false
) : io.antarescircuit.jabbah.animation.AbstractAnimationTask<T>(target, consumer, sequence, duration, dependsOnSystemSpeed)
