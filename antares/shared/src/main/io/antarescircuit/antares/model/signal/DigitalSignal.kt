package io.antarescircuit.antares.model.signal

import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.antares.model.gate.CurrentUndefinedGateInputBehavior

interface DigitalSignal {

	val bitWidth: BitWidth

	val isFullyUndefined: Boolean

	val isPartiallyUndefined: Boolean

	val isZero: Boolean

	val hasError: Boolean

	val bits: List<Bit>

	val hexString: String

	val binaryString: String

	val octalString: String

	val decimalString: String

	val color: CompositeColor

	/** Returns the most significant bit. */
	val msb: Bit get() = bitAt(bitWidth.width - 1)

	/** Returns the least significant bit. */
	val lsb: Bit get() = bitAt(0)

    operator fun not(): DigitalSignal

    /**
     * Returns the bitwise 'and' of this [DigitalSignal] and the specified one.
     * The result has the same [BitWidth] as this [DigitalSignal].
     */
    fun and(signal: DigitalSignal): DigitalSignal

    fun and(value: ULong): DigitalSignal

	/**
	 * Returns the bitwise 'or' of this [DigitalSignal] and the specified one.
	 * The result has the same [BitWidth] as this [DigitalSignal].
	 */
    fun or(signal: DigitalSignal): DigitalSignal

    fun or(value: ULong): DigitalSignal

    fun bitAt(index: Int): Bit

    fun flip(index: Int): DigitalSignal

	/**
	 * Returns the subword of this [DigitalSignal] that has [subwordWidth] and is located at [index].
	 * For example, the 4-bit subword of an 8-bit signal at index 0 are the 4 bits with the least priority,
	 * i.e. bit 0..3.
	 */
    fun getSubword(subwordWidth: BitWidth, index: Int): DigitalSignal

	fun getSubwordValue(subwordWidth: BitWidth, index: Int): ULong?

	/**
	 * Returns the value of a given number of [Bits][Bit] at the specified position in this [DigitalSignal].
	 * In contrast to [getSubword], this method can be used to access "sub values" that are not aligned
	 * with standard [BitWidth] sub ranges.
	 * Uses [CurrentUndefinedGateInputBehavior] to replace undefined [Bits][Bit].
	 * @return `null` if any of the [Bits][Bit] is [Bit.Error]
	 */
	fun bitsAt(pos: Int, size: Int): ULong?

    /**
     * Returns the value of this [DigitalSignal] as an Integer, or `null` if any of the [Bit]s is
     * undefined.
     */
    fun toInt(): Int?

    fun toLong(): ULong?

	/**
	 * Creates a copy of this [DigitalSignal] by replacing all undefined [Bit] according to the
	 * [CurrentUndefinedGateInputBehavior].
	 * @throws IllegalStateException if a [Bit.Error] occurs
	 */
	fun asDefined(): DigitalSignal

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

	fun getValue(): ULong

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

	fun isAllOf(bit: Bit): Boolean

	fun nibbleToHexChar(index: Int): Char

	/**
	 * Creates a new [DigitalSignal] with the sum of this [DigitalSignal] and [other] with this [DigitalSignal]'s [BitWidth].
	 * Sums that don't fit in that [BitWidth] get truncated.
	 */
	fun add(other: UInt): DigitalSignal

	fun subtract(other: UInt): DigitalSignal

	fun multiply(other: UInt): DigitalSignal

	fun divide(other: ULong): DigitalSignal

	/** Modulo operation. */
	fun mod(value: ULong): DigitalSignal

	fun isGreaterThan(value: ULong): Boolean

	fun isGreaterEqualThan(value: ULong): Boolean

	fun isSmallerThan(value: ULong): Boolean

	fun isSmallerEqualThan(value: ULong): Boolean

	fun power(exp: Byte): DigitalSignal
}