package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.draw.graphics.CompositeColor
import kotlin.random.nextULong

/**
 * A [DigitalSignal] implementation containing only defined bits for faster execution that with [Word].
 */
class DefinedWord(
	override val bitWidth: BitWidth,
	longValue: ULong
) : DigitalSignal {

	companion object {

		private val BIT_WIDTH_MASKS = mutableMapOf<BitWidth, ULong>()

		init {
			var mask = 0UL
			var bit = 1UL
			for (i in 1..BitWidth.MAX) {
				mask += bit
				BIT_WIDTH_MASKS[BitWidth.of(i)] = mask
				bit = bit.shl(1)
			}
		}

		private val ZERO = DefinedWord(BitWidth.BW_1, 0UL)
		private val ONE = DefinedWord(BitWidth.BW_1, 1UL)

		fun of(bitWidth: BitWidth, value: ULong): DefinedWord =
			if (bitWidth == BitWidth.BW_1) {
				if (value == 0UL) ZERO else ONE
			} else {
				DefinedWord(bitWidth, value)
			}

		fun of(bitValue: Boolean): DefinedWord = if (bitValue) ONE else ZERO

		fun random(bitWidth: BitWidth): DefinedWord = DefinedWord(bitWidth, kotlin.random.Random.nextULong())
	}

	val longValue: ULong = longValue.and(BIT_WIDTH_MASKS[bitWidth]!!)

	/** ---- [Any] */

	override fun toString(): String = "${longValue}:${bitWidth.width}"

	override fun equals(other: Any?): Boolean {
		if (this === other) return true

		if (other == null) return false

		if (other is Word) {
			return other.bitWidth == bitWidth && other.toLong() == longValue
		}

		other as DefinedWord

		if (bitWidth != other.bitWidth) return false
		if (longValue != other.longValue) return false

		return true
	}

	override fun hashCode(): Int {
		var result = bitWidth.hashCode()
		result = 31 * result + longValue.hashCode()
		return result
	}

	/** ---- [DigitalSignal] */

	override val isFullyUndefined: Boolean get() = false

	override val isPartiallyUndefined: Boolean get() = false

	override val isZero: Boolean get() = longValue == 0UL

	override val hasError: Boolean get() = false

	override val bits: List<Bit> by lazy {
		val bits = mutableListOf<Bit>()
		var v = longValue
		for (i in 0 until bitWidth.width) {
			bits.add(Bit.of(v % 2UL == 1UL))
			v = v.shr(1)
		}
		bits
	}

	override val hexString: String by lazy { BitOperation.longToHexPadded(longValue, bitWidth) }

	override val binaryString: String by lazy { BitOperation.longToBinaryPadded(longValue, bitWidth) }

	override val decimalString: String by lazy { longValue.toString() }

	override val color: CompositeColor by lazy { DigitalSignalColor.ofSignal(this) }

	override fun not(): DigitalSignal = DefinedWord(bitWidth, longValue.inv())

	override fun and(signal: DigitalSignal): DigitalSignal =
		signal.toLong()?.let { DefinedWord(bitWidth, longValue.and(it)) } ?: Word.of(bitWidth, longValue).and(signal)

	override fun and(value: ULong): DigitalSignal =
		DefinedWord(bitWidth, this.longValue.and(value))

	override fun or(signal: DigitalSignal): DigitalSignal =
		signal.toLong()?.let { DefinedWord(bitWidth, longValue.or(it)) } ?: Word.of(bitWidth, longValue).or(signal)

	override fun or(value: ULong): DigitalSignal =
		DefinedWord(bitWidth, this.longValue.or(value))

	override fun bitAt(index: Int): Bit = Bit.of(BitOperation.getBitAt(longValue, index))

	override fun flip(index: Int): DigitalSignal {
		val mask = 1UL.shl(index)
		return if (mask.and(longValue) == 0UL) {
			DefinedWord(bitWidth, longValue.or(mask))
		} else {
			DefinedWord(bitWidth, longValue.and(mask.inv()))
		}
	}

	override fun getSubword(subwordWidth: BitWidth, index: Int): DigitalSignal {
		val mask = (BitOperation.power(subwordWidth.width.toByte()) - 1UL).shl(subwordWidth.width * index)
		return DefinedWord(subwordWidth, longValue.and(mask).shr(subwordWidth.width * index))
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
			DefinedWord(bitWidth, longValue)
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
		DefinedWord(bitWidth, longValue.shl(bitCount))

	override fun shiftRight(bitCount: Int): DigitalSignal =
		DefinedWord(bitWidth, longValue.shr(bitCount))

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
		DefinedWord(bitWidth, longValue + other)

	override fun subtract(other: UInt): DigitalSignal =
		DefinedWord(bitWidth, longValue - other)

	override fun multiply(other: UInt): DigitalSignal =
		DefinedWord(bitWidth, longValue * other)

	override fun divide(other: ULong): DigitalSignal =
		if (other == 0UL) this else DefinedWord(bitWidth, longValue / other)

	override fun mod(value: ULong): DigitalSignal =
		if (value == 0UL) DefinedWord(bitWidth, 0UL) else DefinedWord(bitWidth, longValue.mod(value))

	override fun isGreaterThan(value: ULong): Boolean = longValue > value

	override fun isGreaterEqualThan(value: ULong): Boolean = longValue >= value

	override fun isSmallerThan(value: ULong): Boolean = longValue < value

	override fun isSmallerEqualThan(value: ULong): Boolean = longValue <= value
}