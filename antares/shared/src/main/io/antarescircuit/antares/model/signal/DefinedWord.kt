package io.antarescircuit.antares.model.signal

import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import kotlin.math.pow
import kotlin.random.nextULong

/**
 * A [DigitalSignal] implementation containing only defined bits for faster execution than with [Word].
 */
class DefinedWord(
	override val bitWidth: BitWidth,
	longValue: ULong
) : DigitalSignal {

	companion object {

		/** Masks indexed by width, avoiding boxed map lookups on every construction. */
		private val BIT_WIDTH_MASKS = ULongArray(BitWidth.MAX + 1) { if (it == 64) ULong.MAX_VALUE else (1UL shl it) - 1UL }

		/** Hash shared with fully defined [Word]s, which compare equal to [DefinedWord]s. */
		internal fun hashOf(width: Int, value: ULong): Int = 31 * width + value.hashCode()

		private val ZERO = DefinedWord(BitWidth.BW_1, 0UL)
		private val ONE = DefinedWord(BitWidth.BW_1, 1UL)

		private val WIDTH_2_VALUES = Array(4) { DefinedWord(BitWidth.BW_2, it.toULong()) }

		private val WIDTH_4_VALUES = Array(16) { DefinedWord(BitWidth.BW_4, it.toULong()) }

		private val WIDTH_8_VALUES = Array(256) { DefinedWord(BitWidth.BW_8, it.toULong()) }

		/** Returns a [DefinedWord] with [value] masked to [bitWidth], shared for common small widths. */
		fun of(bitWidth: BitWidth, value: ULong): DefinedWord {
			if (bitWidth is BitWidthImpl) {
				val masked = value and BIT_WIDTH_MASKS[bitWidth.width]
				when (bitWidth.width) {
					1 -> return if (masked == 0UL) ZERO else ONE
					2 -> return WIDTH_2_VALUES[masked.toInt()]
					4 -> return WIDTH_4_VALUES[masked.toInt()]
					8 -> return WIDTH_8_VALUES[masked.toInt()]
				}
			}
			return DefinedWord(bitWidth, value)
		}

		fun of(bitValue: Boolean): DefinedWord = if (bitValue) ONE else ZERO

		fun random(bitWidth: BitWidth): DefinedWord = DefinedWord(bitWidth, kotlin.random.Random.nextULong())
	}

	val longValue: ULong = longValue.and(BIT_WIDTH_MASKS[bitWidth.width])

	/** ---- [Any] */

	override fun toString(): String = "$longValue"

	override fun equals(other: Any?): Boolean {
		if (this === other) return true

		if (other == null) return false

		if (other is Word) {
			return other.bitWidth.width == bitWidth.width && other.toLong() == longValue
		}
		if (other !is DefinedWord) return false

		return bitWidth.width == other.bitWidth.width && longValue == other.longValue
	}

	override fun hashCode(): Int = hashOf(bitWidth.width, longValue)

	/** ---- [DigitalSignal] */

	override val isFullyUndefined: Boolean get() = false

	override val isPartiallyUndefined: Boolean get() = false

	override val isZero: Boolean get() = longValue == 0UL

	override val hasError: Boolean get() = false

	// Caches computed on first use. Plain nullable fields instead of lazy delegates, since a
	// DefinedWord is allocated for most simulation steps and the delegates cost several objects each.
	private var cachedBits: List<Bit>? = null
	private var cachedHex: String? = null
	private var cachedBinary: String? = null
	private var cachedColor: CompositeColor? = null

	override val bits: List<Bit> get() = cachedBits ?: List(bitWidth.width) { Bit.of(BitOperation.getBitAt(longValue, it)) }
		.also { cachedBits = it }

	override val hexString: String get() = cachedHex ?: BitOperation.longToHexPadded(longValue, bitWidth).also { cachedHex = it }

	override val binaryString: String get() =
		cachedBinary ?: BitOperation.longToBinaryPadded(longValue, bitWidth).also { cachedBinary = it }

	override val octalString: String get() = longValue.toString(8)

	override val decimalString: String get() = longValue.toString()

	override val color: CompositeColor get() = cachedColor ?: DigitalSignalColor.ofSignal(this).also { cachedColor = it }

	override fun not(): DigitalSignal = of(bitWidth, longValue.inv())

	override fun and(signal: DigitalSignal): DigitalSignal =
		signal.toLong()?.let { of(bitWidth, longValue.and(it)) } ?: Word.of(bitWidth, longValue).and(signal)

	override fun and(value: ULong): DigitalSignal =
		of(bitWidth, this.longValue.and(value))

	override fun or(signal: DigitalSignal): DigitalSignal =
		signal.toLong()?.let { of(bitWidth, longValue.or(it)) } ?: Word.of(bitWidth, longValue).or(signal)

	override fun or(value: ULong): DigitalSignal =
		of(bitWidth, this.longValue.or(value))

	override fun bitAt(index: Int): Bit = Bit.of(BitOperation.getBitAt(longValue, index))

	override fun flip(index: Int): DigitalSignal {
		val mask = 1UL.shl(index)
		return if (mask.and(longValue) == 0UL) {
			of(bitWidth, longValue.or(mask))
		} else {
			of(bitWidth, longValue.and(mask.inv()))
		}
	}

	override fun getSubword(subwordWidth: BitWidth, index: Int): DigitalSignal {
		if (subwordWidth.width == bitWidth.width && index == 0) {
			return this
		}
		val mask = (BitOperation.power(subwordWidth.width.toByte()) - 1UL).shl(subwordWidth.width * index)
		return of(subwordWidth, longValue.and(mask).shr(subwordWidth.width * index))
	}

	override fun getSubwordValue(subwordWidth: BitWidth, index: Int): ULong =
		getSubword(subwordWidth, index).getValue()

	override fun bitsAt(pos: Int, size: Int): ULong {
		val mask = (BitOperation.power(size.toByte()) - 1UL).shl(pos)
		return longValue.and(mask).shr(pos)
	}

	override fun toInt(): Int = longValue.toInt()

	override fun toLong(): ULong = longValue

	override fun asDefined(): DigitalSignal = this

	override fun replaceBy(replacement: Bit, filter: (Int, Bit) -> Boolean): DigitalSignal =
		Word.of(bitWidth, longValue).replaceBy(replacement, filter)

	override fun isConsistentWith(other: DigitalSignal?): Boolean {
		if (this.bitWidth.width != other?.bitWidth?.width) {
			return false
		}
		if (other is DefinedWord) {
			return this.longValue == other.longValue
		}
		if (other is Word) {
			return other.isConsistentWith(this)
		}
		return Word.of(bitWidth, longValue).isConsistentWith(this)
	}

	override fun defineSubword(subword: DigitalSignal, index: Int): DigitalSignal {
		// Everything already defined, nothing to change
		return this
	}

	override fun ofWidth(bitWidth: BitWidth): DigitalSignal =
		if (this.bitWidth == bitWidth) {
			this
		} else {
			of(bitWidth, longValue)
		}

	override fun withBit(index: Int, bit: Bit): DigitalSignal =
		if (bit.isDefined) {
			if (bitAt(index) == bit) {
				this
			} else {
				flip(index)
			}
		} else {
			Word.of(bitWidth, longValue).withBit(index, bit)
		}

	override fun getValue(): ULong = longValue

	override fun withSubwordValue(subword: DigitalSignal, index: Int): DigitalSignal =
		Word.of(bitWidth, longValue).withSubwordValue(subword, index)

	override fun shiftLeft(bitCount: Int): DigitalSignal =
		of(bitWidth, longValue.shl(bitCount))

	override fun shiftRight(bitCount: Int): DigitalSignal =
		of(bitWidth, longValue.shr(bitCount))

	override fun isAllOf(bit: Bit): Boolean =
		if (bit.isDefined) {
			if (bit.isSet) {
				longValue == bitWidth.maxValue
			} else {
				longValue == 0UL
			}
		} else {
			false
		}

	override fun nibbleToHexChar(index: Int): Char =
		BitOperation.hexDigit(getSubwordValue(BitWidth.BW_4, index).toLong())

	override fun add(other: UInt): DigitalSignal =
		of(bitWidth, longValue + other)

	override fun subtract(other: UInt): DigitalSignal =
		of(bitWidth, longValue - other)

	override fun multiply(other: UInt): DigitalSignal =
		of(bitWidth, longValue * other)

	override fun divide(other: ULong): DigitalSignal =
		if (other == 0UL) this else of(bitWidth, longValue / other)

	override fun mod(value: ULong): DigitalSignal =
		if (value == 0UL) of(bitWidth, 0UL) else of(bitWidth, longValue.mod(value))

	override fun isGreaterThan(value: ULong): Boolean = longValue > value

	override fun isGreaterEqualThan(value: ULong): Boolean = longValue >= value

	override fun isSmallerThan(value: ULong): Boolean = longValue < value

	override fun isSmallerEqualThan(value: ULong): Boolean = longValue <= value

	override fun power(exp: Byte): DigitalSignal =
		of(bitWidth, longValue.toDouble().pow(exp.toInt()).toULong())
}