package ch.scorpion.antares.model.signal

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.Themes
import kotlin.math.max
import kotlin.math.min

/**
 * A [Word] is the default [DigitalSignal] implementation consisting of multiple [Bit]s.
 *
 * @property bits Holds all [Bit]s of this [Word], with the least priority [Bit] at index `0`.
 */
data class Word(val bits: List<Bit>) : DigitalSignal {

	/** Holds the width of this [Word], i.e. the number of [Bit]s it contains. */
	private val bitWidth: BitWidth = BitWidth.of(bits.size)

	/** Is automatically set to ´true´ if all [Bit]s are [Bit.False]. */
	private val zero: Boolean = bits.all { it == Bit.False }

	/** Is automatically set to ´true´ if all [Bit]s are [Bit.Undefined]. */
	private val undefined: Boolean = bits.all { it == Bit.Undefined }

	companion object {

		private val UNDEFINED: MutableMap<BitWidth, Word> = mutableMapOf()
		private val ERROR: MutableMap<BitWidth, Word> = mutableMapOf()
		private val FALSE: MutableMap<BitWidth, Word> = mutableMapOf()
		private val TRUE: MutableMap<BitWidth, Word> = mutableMapOf()
		private val ZERO_WIDTH_1 = Word(listOf(Bit.False))
		private val ONE_WIDTH_1 = Word(listOf(Bit.True))

		init {
			for (bitWidth in BitWidth.values()) {
				UNDEFINED[bitWidth] = Word(createListWithBit(bitWidth, Bit.Undefined))
				ERROR[bitWidth] = Word(createListWithBit(bitWidth, Bit.Error))
				FALSE[bitWidth] = Word(createListWithBit(bitWidth, Bit.False))
				TRUE[bitWidth] = Word(createListWithBit(bitWidth, Bit.True))
			}
		}

		/** Returns a [Word] of the specified [BitWidth] with all [Bit]s undefined. */
		fun undefined(bitWidth: BitWidth): Word {
			return UNDEFINED[bitWidth]!!
		}

		/** Returns a [Word] of the specified [BitWidth] with all [Bit]s in error state. */
		fun error(bitWidth: BitWidth): Word {
			return ERROR[bitWidth]!!
		}

		/** Returns a [Word] of the specified [BitWidth] with all [Bit]s in `false` state. */
		fun falseValue(bitWidth: BitWidth): Word {
			return FALSE[bitWidth]!!
		}

		/** Returns a [Word] of the specified [BitWidth] with all [Bit]s in `true` state. */
		fun trueValue(bitWidth: BitWidth): Word {
			return TRUE[bitWidth]!!
		}

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
		fun of(bitValue: Boolean): Word {
			return if (bitValue) ONE_WIDTH_1 else ZERO_WIDTH_1
		}

		/** Returns a [Word] consisting of a single [Bit] with the specified value.*/
		fun of(bit: Bit): Word {
			return when (bit) {
				Bit.Undefined -> undefined(BitWidth.BW_1)
				Bit.Error -> error(BitWidth.BW_1)
				Bit.False -> falseValue(BitWidth.BW_1)
				Bit.True -> trueValue(BitWidth.BW_1)
			}
		}

		/** Returns a [Word] that represents the specified value as a binary word of the specified width.*/
		fun of(bitWidth: BitWidth, value: Long?): Word {
			if (value == null) {
				return undefined(bitWidth)
			}
			return Word((0 until bitWidth.width).map { Bit.of(BitOperation.getBitAt(value, it)) })
		}

		/** Combines the specified [Word]s into a single [Word].*/
		fun of(words: List<Word>): Word {
			val list = ArrayList<Bit>()
			for ((bits) in words) {
				list.addAll(bits)
			}
			return Word(list)
		}

		/** Returns the [Word] representing the specified hexadecimal value.*/
		fun of(bitWidth: BitWidth, hexValue: String): Word {
			if (hexValue.all { it == Bit.UNDEFINED_CHAR }) {
				return undefined(bitWidth)
			}
			return of(bitWidth, BitOperation.hexToLong(hexValue))
		}

		/** Creates a list with all the same [Bit]s of the length as defined by the specified [BitWidth].*/
		private fun createListWithBit(bitWidth: BitWidth, bit: Bit): List<Bit> {
			val list = mutableListOf<Bit>()
			for (i in 0 until bitWidth.width) {
				list.add(bit)
			}
			return list
		}
	}

	/** ---- [Any] */

	override fun toString(): String {
		return toHexString()
	}

	/** ---- [DigitalSignal] interface */

	override fun toHexString(): String {
		val sb = StringBuilder()
		for (i in max(0, bits.size / 4 - 1) downTo 0) {
			val nibble: Long? = getSubwordValue(BitWidth.BW_4, i)
			if (nibble == null) {
				sb.append("?")
			} else {
				// Long.toString(radix:Int) is only supported on JVM
				// sb.append(nibble!!.toString(16).toUpperCase())
				sb.append(BitOperation.hexDigit(nibble))
			}
		}
		return sb.toString().padStart(bitWidth.width / 4, '0')
	}

	override fun toBinaryString(): String {
		val sb = StringBuilder()
		for (i in bits.size - 1 downTo 0) {
			sb.append(bits[i].toBinaryString())
		}
		return sb.toString()
	}

	override fun getColor(): CompositeColor {
		if (bitWidth == BitWidth.BW_1) {
			return bitAt(0).color
		}
		if (zero) {
			return Themes.get<AntaresTheme>().wordZero
		}
		if (undefined) {
			return Themes.get<AntaresTheme>().undefined
		}
		return Themes.get<AntaresTheme>().word
	}

	override fun getBitWidth(): BitWidth {
		return bitWidth
	}

	override fun not(): DigitalSignal = Word((0 until getBitWidth().width).map { bitAt(it).not() })

	override fun flip(index: Int): DigitalSignal {
		return Word((0 until getBitWidth().width).map {
			if (it == index) bitAt(it).not() else bitAt(it)
		})
	}

	override fun and(signal: DigitalSignal): DigitalSignal = Word((0 until getBitWidth().width).map { bitAt(it).and(signal.bitAt(it)) })

	override fun bitAt(index: Int): Bit {
		return bits[index]
	}

	override fun getSubword(subwordWidth: BitWidth, index: Int): Word {
		return of(subwordWidth, getSubwordValue(subwordWidth, index))
	}

	override fun toInt(): Int? {
		var value = 0
		var factor = 1
		for (bit in bits) {
			if (!bit.isDefined) {
				return null
			}
			if (bit.isSet) {
				value += factor
			}
			factor *= 2
		}
		return value
	}

	/** ---- [Word] */

	fun getValue(): Long {
		return getSubwordValue(bitWidth, 0)!!
	}

	fun isAllOf(bit: Bit): Boolean {
		return bits.all { it == bit }
	}

	/**
	 * Creates a copy of this [Word] and sets the specified [Bit] in the copy.
	 * @param index the index of the [Bit] to set.
	 * @param bit the [Bit] to set at index [index].
	 * @return the copies and adjusted [Word].
	 */
	fun withBit(index: Int, bit: Bit): Word {
		val bits = mutableListOf<Bit>()
		bits.addAll(this.bits)
		bits[index] = bit
		return Word(bits)
	}

	fun getSubwordValue(subwordWidth: BitWidth, index: Int): Long? {
		var sum: Long = 0
		var digit = min(bitWidth.width, subwordWidth.width) - 1
		for (i in index * subwordWidth.width + digit downTo index * subwordWidth.width) {
			if (!bits[i].isDefined) {
				return null
			}
			if (bits[i].numericalValue == 1) {
				sum += BitOperation.power(digit.toByte())
			}
			digit--
		}
		return sum
	}

	/**
	 * Creates a copy of this [Word] and sets the specified sub-word in the copy.
	 *
	 * @param subword the [Word] to be set in the copy of this [Word]
	 * @param index the index of the replaced sub-word. For example, an 8-Bit word consists of
	 * two sub-words with index 0 (bits 0..3) and index 1 (bits 4..7)
	 */
	fun withSubwordValue(subword: Word, index: Int): Word {
		val resultBits = mutableListOf<Bit>()
		resultBits.addAll(this.bits)

		val minBitIndex = index * subword.bitWidth.width
		var subwordIndex = 0
		if (minBitIndex < this.bitWidth.width) {
			for (resultIndex in minBitIndex..min(bitWidth.width - 1, minBitIndex + subword.bitWidth.width - 1)) {
				resultBits[resultIndex] = subword.bitAt(subwordIndex++)
			}
		}
		return Word(resultBits)
	}

	fun containsUndefinedBit(): Boolean {
		return bits.any { it == Bit.Undefined }
	}

	/**
	 * Creates a copy of this [Word]'s value and expands or reduces it to the specified [BitWidth].
	 * Expanding is done by adding zeros to the left, while reducing is done by truncating high order numbers.
	 */
	fun ofWidth(bitWidth: BitWidth): Word {
		return of(bitWidth, getValue())
	}

	fun shiftLeft(bitCount: Int = 1): Word {
		return of(bitWidth, getValue().shl(bitCount))
	}

	fun shiftRight(bitCount: Int = 1): Word {
		return of(bitWidth, getValue().shr(bitCount))
	}
}