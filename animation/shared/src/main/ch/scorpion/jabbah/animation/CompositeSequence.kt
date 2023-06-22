package ch.scorpion.jabbah.animation

/**
 * Combines multiple [Sequence]s of the same type into a single one that executes the elements one after another.
 * @param T the type of values produced by this [CompositeSequence].
 */
open class CompositeSequence<out T : Any>(vararg sequences: Sequence<T>) : Sequence<T> {

    private val sequences = sequences.toList()
    private var currentSequenceIndex: Int = 0

    /** ---- [Sequence] interface */

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