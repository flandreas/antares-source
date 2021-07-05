package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.draw.graphics.CompositeColor

interface DigitalSignal {

	val bitWidth: BitWidth

	val isFullyUndefined: Boolean

	val isPartiallyUndefined: Boolean

	val bits: List<Bit>

    fun toHexString(): String

    fun toDecimalString(): String

    fun toBinaryString(): String

    fun getColor(): CompositeColor

    operator fun not(): DigitalSignal

    /** Returns the bitwise 'and' of this [DigitalSignal] and the specified one.*/
    fun and(signal: DigitalSignal): DigitalSignal

    fun bitAt(index: Int): Bit

    fun flip(index: Int): DigitalSignal

    fun getSubword(subwordWidth: BitWidth, index: Int): DigitalSignal

	fun getSubwordValue(subwordWidth: BitWidth, index: Int): Long?

    /**
     * Returns the value of this [DigitalSignal] as an Integer, or `null` if any of the [Bit]s is
     * undefined.
     */
    fun toInt(): Int?

    fun toLong(): Long?

	/**
	 * Creates a copy of this [DigitalSignal] by replacing all [Bit]s with [replacement]
	 * that fulfill condition [filter].
	 *
	 * @param filter receives [Bit]s of this [DigitalSignal] as input
	 */
	fun replaceBy(replacement: Bit, filter: (Int, Bit) -> Boolean): DigitalSignal

	/**
	 * Two [DigitalSignal]s are consistent they have the same [BitWidth] and every non-undefined [Bit] is equal.
	 */
	fun isConsistentWith(other: DigitalSignal?): Boolean

	/**
	 * Creates a copy of this [DigitalSignal] and used all [Bit]s of [subword] where the corresponding
	 * [Bit] in this [DigitalSignal] is [Bit.Undefined].
	 */
	fun defineSubword(subword: DigitalSignal, index: Int): DigitalSignal

	/**
	 * Creates a copy of this [DigitalSignal]'s value and expands or reduces it to the specified [BitWidth].
	 * Expanding is done by adding zeros to the left, while reducing is done by truncating high order numbers.
	 */
	fun ofWidth(bitWidth: BitWidth): DigitalSignal

	/**
	 * Creates a copy of this [DigitalSignal] and sets the specified [Bit] in the copy.
	 * @param index the index of the [Bit] to set.
	 * @param bit the [Bit] to set at index [index].
	 * @return the copied and adjusted [DigitalSignal].
	 */
	fun withBit(index: Int, bit: Bit): DigitalSignal

	fun getValue(): Long

	/**
	 * Creates a copy of this [DigitalSignal] and sets the specified sub-word in the copy.
	 * Note that this method works only for [DigitalSignalRepresentation]s with fully aligned nibbles.
	 * In particular, it doesn't work with [DigitalSignalRepresentation.DECIMAL].
	 *
	 * @param subword the [DigitalSignal] to be set in the copy of this [DigitalSignal]
	 * @param index the index of the replaced sub-word. For example, an 8-Bit word consists of
	 * two sub-words with index 0 (bits 0..3) and index 1 (bits 4..7)
	 */
	fun withSubwordValue(subword: DigitalSignal, index: Int): DigitalSignal

	fun shiftLeft(bitCount: Int = 1): DigitalSignal

	fun shiftRight(bitCount: Int = 1): DigitalSignal

	fun containsUndefinedBit(): Boolean

	fun containsErrorBit(): Boolean

	fun isAllOf(bit: Bit): Boolean

	fun nibbleToHexChar(index: Int): Char
}