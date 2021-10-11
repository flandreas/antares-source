package ch.scorpion.jabbah.animation

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

    /** Determines whether this [Sequence] can delivery another value.*/
    fun hasNext(): Boolean

    /** Returns the next value of this [Sequence] and makes it the current one.*/
    fun getNext(distance: Double): T

    /** Returns the value that has been returned by the most recent call of [getNext].*/
    fun getCurrent(): T
}