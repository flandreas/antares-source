package ch.scorpion.jabbah.animation

/**
 * Combines multiple [Sequence]s of the same type into a single one that executes the elements one after another.
 * @param T the type of values produced by this [CompositeSequence].
 */
open class CompositeSequence<out T : Any>(vararg sequences: Sequence<T>) : Sequence<T> {

    private val sequences = sequences.toList()
    private var currentSequenceIndex: Int = 0

    /** ---- [Sequence] interface */

    override val size: Double get() = sequences.map { it.size }.sum()

    override fun hasNext(): Boolean {
        return sequences[currentSequenceIndex].hasNext() || currentSequenceIndex < sequences.size - 1
    }

    override fun getNext(distance: Double): T {
        if (sequences[currentSequenceIndex].hasNext()) {
			return sequences[currentSequenceIndex].getNext(distance)
		}
		if (currentSequenceIndex < sequences.size - 1) {
			currentSequenceIndex++
			return sequences[currentSequenceIndex].getNext(distance)
		}
		throw NoSuchElementException()
    }

    override fun getCurrent(): T {
        return sequences[currentSequenceIndex].getCurrent()
    }
}