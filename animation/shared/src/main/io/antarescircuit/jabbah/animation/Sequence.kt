package io.antarescircuit.jabbah.animation

/**
 * Represents a sequence of values of a particular value type.
 *
 * [Sequence]s with a clearly defined begin and end range should always return
 * the range's end value even [size] divided by distance in [getNext] doesn't fall
 * exactly on the range's end.
 */
interface Sequence<out T> {

    /** Returns the distance between the first and the last value returned by this [Sequence].*/
    val size: Double

    /**
     * Returns the next value of this [Sequence] and makes it the current one,
     * or `null` if there is no next value.
     */
    fun getNext(distance: Double): T?

    /** Returns the value that has been returned by the most recent call of [getNext].*/
    fun getCurrent(): T?
}

/**
 * A [Sequence] capable of creating a reversed clone of itself. which is used for building
 * oscillating composite [Sequence]s.
 * Created clones must be reset to their initial i.e. start state.
 */
interface CloneableSequence<out T> : Sequence<T> {
	fun clone(reversed: Boolean): CloneableSequence<T>
}