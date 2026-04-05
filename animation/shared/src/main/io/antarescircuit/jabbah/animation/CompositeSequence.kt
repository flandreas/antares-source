package io.antarescircuit.jabbah.animation

/**
 * Combines multiple [io.antarescircuit.jabbah.animation.Sequence]s of the same type into a single one that executes the elements one after another.
 * @param T the type of values produced by this [CompositeSequence].
 */
open class CompositeSequence<out T : Any>(vararg sequences: io.antarescircuit.jabbah.animation.Sequence<T>) :
    io.antarescircuit.jabbah.animation.Sequence<T> {

    private val sequences = sequences.toList()
    private var currentSequenceIndex: Int = 0

    /** ---- [io.antarescircuit.jabbah.animation.Sequence] interface */

    override val size: Double get() = sequences.sumOf { it.size }

    override fun getNext(distance: Double): T? {
		val next = sequences[currentSequenceIndex].getNext(distance)
	    if (next != null) {
			return next
	    }
	    if (currentSequenceIndex < sequences.size - 1) {
		    currentSequenceIndex++
		    return sequences[currentSequenceIndex].getNext(distance)
	    }
	    return null
    }

    override fun getCurrent(): T? = sequences[currentSequenceIndex].getCurrent()
}