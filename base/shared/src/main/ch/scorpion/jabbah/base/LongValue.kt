package ch.scorpion.jabbah.base

/**
 * Implemented in higher layers by classes that support math [String] expressions returning a [Long].
 */
interface LongValue {
    val value: Long
}

/**
 * Standard [LongValue] implementation simply containing a [Long].
 */
class LongValueImpl(
    override val value: Long
) : LongValue {

    override fun toString(): String = value.toString()
}