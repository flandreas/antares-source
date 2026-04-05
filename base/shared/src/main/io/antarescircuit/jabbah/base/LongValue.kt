package io.antarescircuit.jabbah.base

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

    companion object {
        val ZERO = LongValueImpl(0L)
        val ONE = LongValueImpl(1L)
    }

    override fun toString(): String = value.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LongValueImpl

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}