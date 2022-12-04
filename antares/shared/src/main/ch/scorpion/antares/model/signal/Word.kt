package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.gate.CurrentUndefinedGateInputBehavior
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import kotlin.math.max
import kotlin.math.min

/**
 * A [Word] is the default [DigitalSignal] implementation consisting of multiple [Bit]s.
 *
 * @property bits Holds all [Bit]s of this [Word], with the least priority [Bit] at index `0`.
 */
internal data class Word(
	override val bits: List<Bit>
) : DigitalSignal {

	companion object {

		private val UNDEFINED: MutableMap<Int, Word> = mutableMapOf()
		private val ERROR: MutableMap<Int, Word> = mutableMapOf()
		private val FALSE: MutableMap<Int, Word> = mutableMapOf()
		private val TRUE: MutableMap<Int, Word> = mutableMapOf()
		private val ZERO_WIDTH_1 = Word(listOf(Bit.False))
		private val ONE_WIDTH_1 = Word(listOf(Bit.True))

		init {
			for (bitWidth in BitWidth.PREDEFINED) {
				UNDEFINED[bitWidth.width] = Word(createListWithBit(bitWidth, Bit.Undefined))
				ERROR[bitWidth.width] = Word(createListWithBit(bitWidth, Bit.Error))
				FALSE[bitWidth.width] = Word(createListWithBit(bitWidth, Bit.False))
				TRUE[bitWidth.width] = Word(createListWithBit(bitWidth, Bit.True))
			}
		}

		/** Returns a [Word] of the specified [BitWidth] with all [Bit]s undefined. */
		fun undefined(bitWidth: BitWidth): Word = UNDEFINED[bitWidth.width]!!

		/** Returns a [Word] of the specified [BitWidth] with all [Bit]s in error state. */
		fun error(bitWidth: BitWidth): Word = ERROR[bitWidth.width]!!

		/** Returns a [Word] of the specified [BitWidth] with all [Bit]s in `false` state. */
		fun falseValue(bitWidth: BitWidth): Word = FALSE[bitWidth.width]!!

		/** Returns a [Word] of the specified [BitWidth] with all [Bit]s in `true` state. */
		fun trueValue(bitWidth: BitWidth): Word = TRUE[bitWidth.width]!!

		/** Returns a [Word] of the specified width with all the same [Bit]s.*/
		fun allOf(bitWidth: BitWidth, bit: Bit): Word {
			return when (bit) {
				Bit.Undefined -> undefined(bitWidth)
				Bit.Error -> error(bitWidth)
				Bit.False -> falseValue(bitWidth)
				Bit.True -> trueValue(bitWidth)
			}
		}

		/** Returns a [Word] consisting of a single [Bit] with the specified value.*/
		fun of(bitValue: Boolean): Word = if (bitValue) ONE_WIDTH_1 else ZERO_WIDTH_1

		/** Returns a [Word] consisting of a single [Bit] with the specified value.*/
		fun of(bit: Bit): Word = when (bit) {
			Bit.Undefined -> undefined(BitWidth.BW_1)
			Bit.Error -> error(BitWidth.BW_1)
			Bit.False -> falseValue(BitWidth.BW_1)
			Bit.True -> trueValue(BitWidth.BW_1)
		}

		/** Returns a [Word] that represents the specified value as a binary word of the specified width.*/
		fun of(bitWidth: BitWidth, value: ULong?): Word {
			if (value == null) {
				return undefined(bitWidth)
			}
			return Word((0 until bitWidth.width).map { Bit.of(BitOperation.getBitAt(value, it)) })
		}

		fun ofMinimalBitWidth(value: ULong): Word =
			of(
				BitWidth.PREDEFINED.firstOrNull { it.maxValue >= value }  ?: throw IllegalArgumentException(""),
				value)

		/** Combines the specified [Word]s into a single [Word].*/
		fun of(words: List<DigitalSignal>): Word {
			val list = ArrayList<Bit>()
			words.forEach { list.addAll(it.bits) }
			return Word(list)
		}

		/** Returns the [Word] representing the specified hexadecimal value.*/
		fun of(bitWidth: BitWidth, hexValue: String): Word {
			if (hexValue.all { it == Bit.ALL_UNDEFINED_CHAR }) {
				return undefined(bitWidth)
			}
			return of(bitWidth, BitOperation.hexToLong(hexValue))
		}

		fun random(bitWidth: BitWidth): Word {
			val list = mutableListOf<Bit>()
			for (i in 0 until bitWidth.width) {
				list.add(Bit.random())
			}
			return Word(list)
		}

		/** Creates a list with all the same [Bit]s of the length as defined by the specified [BitWidth].*/
		fun createListWithBit(bitWidth: BitWidth, bit: Bit): List<Bit> {
			val list = mutableListOf<Bit>()
			for (i in 0 until bitWidth.width) {
				list.add(bit)
			}
			return list
		}
	}

	override val bitWidth: BitWidth = BitWidth.of(bits.size)

	/** Is ´true´ if all [Bit]s are [Bit.False]. */
	override val isZero: Boolean by lazy { bits.all { it == Bit.False } }

	/** Is ´true´ if any [Bit]s is [Bit.Undefined]. */
	private val undefined: Boolean by lazy { bits.any { it == Bit.Undefined } }

	private val error: Boolean by lazy { bits.any { it == Bit.Error } }

	private val allDefined: Boolean by lazy { bits.all { it.isDefined } }

	/** ---- [Any] */

	override fun toString(): String {
		return if (isPartiallyUndefined) {
			binaryString
		} else {
			hexString
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true

		if (other == null) return false

		if (other is DefinedWord) {
			return bitWidth == other.bitWidth && longValue == other.longValue
		}

		other as Word

		if (bits != other.bits) return false
		if (bitWidth != other.bitWidth) return false

		return true
	}

	override fun hashCode(): Int {
		var result = bits.hashCode()
		result = 31 * result + bitWidth.hashCode()
		return result
	}

	/** ---- [DigitalSignal] interface */

	override val hexString: String by lazy {
		val sb = StringBuilder()
		for (i in max(0, bits.size / 4 - 1) downTo 0) {
			sb.append(nibbleToHexChar(i))
		}
		sb.toString().padStart(bitWidth.width / 4, '0')
	}

	override val isFullyUndefined: Boolean by lazy { isAllOf(Bit.Undefined) }

	override val isPartiallyUndefined: Boolean get() = undefined

	override val hasError: Boolean get() = error

	override val binaryString: String by lazy {
		val sb = StringBuilder()
		for (i in bits.size - 1 downTo 0) {
			sb.append(bits[i].toBinaryString())
		}
		sb.toString()
	}

	override val decimalString: String by lazy {
		toLong()?.toString() ?:
		if (isPartiallyUndefined) Bit.ALL_UNDEFINED_CHAR.toString() else Bit.ERROR_CHAR.toString()
	}

	override val color: CompositeColor by lazy { DigitalSignalColor.ofSignal(this) }

	private val notValue: DigitalSignal by lazy {
		Word((0 until bitWidth.width).map { bitAt(it).not() })
	}

	override fun not(): DigitalSignal = notValue

	override fun flip(index: Int): DigitalSignal =
		Word((0 until bitWidth.width).map {
			if (it == index) bitAt(it).not() else bitAt(it)
		})

	override fun and(signal: DigitalSignal): DigitalSignal =
		Word((0 until bitWidth.width).map { bitAt(it).and(signal.bitAt(it)) })

	override fun and(value: ULong): DigitalSignal = and(of(bitWidth, value))

	override fun or(signal: DigitalSignal): DigitalSignal =
		Word((0 until bitWidth.width).map { bitAt(it).or(signal.bitAt(it)) })

	override fun or(value: ULong): DigitalSignal = or(of(bitWidth, value))

	override fun bitAt(index: Int): Bit = bits[index]

	private val longValue: ULong? by lazy calcLong@ {
		var value = 0UL
		var factor = 1UL
		for (bit in bits) {
			if (!bit.isDefined) {
				return@calcLong null
			}
			if (bit.isSet) {
				value += factor
			}
			factor *= 2UL
		}
		return@calcLong value
	}

	override fun toLong(): ULong? = longValue

	override fun toInt(): Int? = toLong()?.toInt()

	override fun getValue(): ULong = longValue!!

	override fun isAllOf(bit: Bit): Boolean = bits.all { it == bit }

	override fun withBit(index: Int, bit: Bit): DigitalSignal {
		val bits = mutableListOf<Bit>()
		bits.addAll(this.bits)
		bits[index] = bit
		return Word(bits)
	}

	override fun getSubword(subwordWidth: BitWidth, index: Int): DigitalSignal {
		if (subwordWidth == bitWidth && index == 0) {
			return this
		}
		if (allDefined) {
			// Tuning
			return DefinedWord.of(bitWidth, longValue!!).getSubword(subwordWidth, index)
		}

		// Use the same loop structure as in getSubWordValue
		val subword = mutableListOf<Bit>()
		val digit = min(bitWidth.width, subwordWidth.width) - 1
		for (i in index * subwordWidth.width + digit downTo index * subwordWidth.width) {
			if (i < bits.size) {
				subword.add(0, bits[i])
			} else {
				subword.add(0, Bit.False)
			}
		}
		// Left pad
		/*
		while (subword.size < subwordWidth.width) {
			subword.add(Bit.False)
		}
		*/
		return Word(subword)
	}

	override fun getSubwordValue(subwordWidth: BitWidth, index: Int): ULong? {
		var sum: ULong = 0UL
		var digit = min(bitWidth.width, subwordWidth.width) - 1
		for (i in index * subwordWidth.width + digit downTo index * subwordWidth.width) {
			if (i < bits.size) {
				if (!bits[i].isDefined) {
					return null
				}
				if (bits[i].numericalValue == 1) {
					sum += BitOperation.power(digit.toByte())
				}
			}
			digit--
		}
		return sum
	}

	override fun bitsAt(pos: Int, size: Int): ULong? {
		var result = 0UL
		var factor = 1UL
		for (i in pos..min(pos + size, bits.size - 1)) {
			val bit = when (bits[i]) {
				Bit.Undefined -> CurrentUndefinedGateInputBehavior.value.definedBit
				Bit.Error -> return null
				Bit.True, Bit.False -> bits[i]
			}
			result += factor * bit.numericalValue.toULong()
			factor *= 2UL
		}
		return result
	}

	override fun nibbleToHexChar(index: Int): Char {
		val subword = getSubword(BitWidth.BW_4, index)
		return when {
			subword.isAllOf(Bit.Undefined) -> Bit.ALL_UNDEFINED_CHAR
			subword.hasError -> Bit.ERROR_CHAR
			subword.isPartiallyUndefined -> Bit.SOME_UNDEFINED_CHAR
			// Long.toString(radix:Int) is only supported on JVM
			else -> BitOperation.hexDigit(subword.toInt()!!.toLong())
		}
	}

	override fun withSubwordValue(subword: DigitalSignal, index: Int): DigitalSignal {
		val resultBits = mutableListOf<Bit>()
		resultBits.addAll(this.bits)
		replaceFromSubword(resultBits, subword, index)
		return Word(resultBits)
	}

	private fun replaceFromSubword(resultBits: MutableList<Bit>, subword: DigitalSignal, index: Int, condition: (Int) -> Boolean = { true }) {
		val minBitIndex = index * subword.bitWidth.width
		var subwordIndex = 0
		if (minBitIndex < this.bitWidth.width) {
			for (resultIndex in minBitIndex..min(bitWidth.width - 1, minBitIndex + subword.bitWidth.width - 1)) {
				if (condition(resultIndex)) {
					resultBits[resultIndex] = subword.bitAt(subwordIndex)
				}
				subwordIndex++
			}
		}
	}

	override fun defineSubword(subword: DigitalSignal, index: Int): Word {
		val resultBits = mutableListOf<Bit>()
		resultBits.addAll(this.bits)
		replaceFromSubword(resultBits, subword, index) { resultBits[it] == Bit.Undefined }
		return Word(resultBits)
	}

	override fun asDefined(): DigitalSignal {
		val resultBits = mutableListOf<Bit>()
		for (bit in bits) {
			when (bit) {
				Bit.True, Bit.False -> resultBits.add(bit)
				Bit.Undefined -> resultBits.add(CurrentUndefinedGateInputBehavior.value.definedBit)
				Bit.Error -> throw IllegalStateException("bit has error value")
			}
		}
		return Word(resultBits)
	}

	override fun ofWidth(bitWidth: BitWidth): DigitalSignal = of(bitWidth, getValue())

	override fun shiftLeft(bitCount: Int): DigitalSignal {
		val newBits = bits.toMutableList()
		for (i in 0 until bitCount) {
			newBits.add(0, Bit.False)
			newBits.removeLast()
		}
		return Word(newBits)
	}

	override fun shiftRight(bitCount: Int): DigitalSignal {
		val newBits = bits.toMutableList()
		for (i in 0 until bitCount) {
			newBits.removeFirst()
			newBits.add(Bit.False)
		}
		return Word(newBits)
	}

	override fun replaceBy(replacement: Bit, filter: (Int, Bit) -> Boolean): Word {
		val resultBits = mutableListOf<Bit>()
		bits.forEachIndexed { index, bit ->
			if (filter.invoke(index, bit)) {
				resultBits.add(replacement)
			} else {
				resultBits.add(bit)
			}
		}
		return Word(resultBits)
	}

	override fun isConsistentWith(other: DigitalSignal?): Boolean {
		if (this.bitWidth.width != other?.bitWidth?.width) {
			return false
		}
		bits.forEachIndexed { index, bit ->
			if (!bit.isConsistentWith(other.bitAt(index))) {
				return false
			}
		}
		return true
	}

	override fun add(other: UInt): DigitalSignal =
		of(bitWidth, (longValue ?: asDefined().getValue()) + other)

	override fun subtract(other: UInt): DigitalSignal =
		of(bitWidth, (longValue ?: asDefined().getValue()) - other)

	override fun multiply(other: UInt): DigitalSignal =
		of(bitWidth, (longValue ?: asDefined().getValue()) * other)

	override fun divide(other: ULong): DigitalSignal =
		if (other == 0UL) {
			this
		} else {
			of(bitWidth, (longValue ?: asDefined().getValue()) / other)
		}

	override fun mod(value: ULong): DigitalSignal =
		if (value == 0UL) {
			of(bitWidth, 0UL)
		} else {
			of(bitWidth, (longValue ?: asDefined().getValue()).mod(value))
		}

	override fun isGreaterThan(value: ULong): Boolean =
		(longValue ?: asDefined().getValue()) > value

	override fun isGreaterEqualThan(value: ULong): Boolean =
		(longValue ?: asDefined().getValue()) >= value

	override fun isSmallerThan(value: ULong): Boolean =
		(longValue ?: asDefined().getValue()) < value

	override fun isSmallerEqualThan(value: ULong): Boolean =
		(longValue ?: asDefined().getValue()) <= value
}