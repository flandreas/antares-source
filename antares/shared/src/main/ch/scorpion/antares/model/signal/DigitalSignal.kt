package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.draw.graphics.CompositeColor

interface DigitalSignal {

	val isFullyUndefined: Boolean

	val isPartiallyUndefined: Boolean

    fun toHexString(): String

    fun toBinaryString(): String

    fun getColor(): CompositeColor

    fun getBitWidth(): BitWidth

    operator fun not(): DigitalSignal

    /** Returns the bitwise 'and' of this [DigitalSignal] and the specified one.*/
    fun and(signal: DigitalSignal): DigitalSignal

    fun bitAt(index: Int): Bit

    fun flip(index: Int): DigitalSignal

    fun getSubword(subwordWidth: BitWidth, index: Int): Word

    /**
     * Returns the value of this [DigitalSignal] as an Integer, or `null` if any of the [Bit]s is
     * undefined.
     */
    fun toInt(): Int?

	/**
	 * Creates a copy of this [DigitalSignal] by replacing all [Bit]s with [replacement]
	 * that fulfill condition [filter].
	 *
	 * @param filter receives [Bit]s of this [DigitalSignal] as input
	 */
	fun replaceBy(replacement: Bit, filter: (Int, Bit) -> Boolean): Word

	/**
	 * Two [DigitalSignal]s are consistent they have the same [BitWidth] and every non-undefined [Bit] is equal.
	 */
	fun isConsistentWith(other: DigitalSignal?): Boolean

	/**
	 * Creates a copy of this [DigitalSignal] and used all [Bit]s of [subword] where the corresponding
	 * [Bit] in this [DigitalSignal] is [Bit.Undefined].
	 */
	fun defineSubword(subword: DigitalSignal, index: Int): DigitalSignal
}