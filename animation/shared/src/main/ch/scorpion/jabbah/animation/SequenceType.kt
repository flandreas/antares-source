package ch.scorpion.jabbah.animation

/**
 * Determines the possible behaviours of a [Sequence] after the last value has been returned.
 */
enum class SequenceType {

    /** The [Sequence] is traversed only once, i.e it ends after returning the last value.*/
    ONCE,

    /** The [Sequence] is traversed forever. It returns again the first value after it has returned the last value. */
    LOOP,

    /**
     * The [Sequence] is traversed forever. After the last value has been reached, the [Sequence] is traversed in
     * the opposite direction, and vice versa.
     */
    OSCILLATION
}