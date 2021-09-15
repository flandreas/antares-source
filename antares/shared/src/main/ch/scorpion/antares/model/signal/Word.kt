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
internal data class Word(
	override val bits: List<Bit>
) : DigitalSignal {

	override val bitWidth: BitWidth = BitWidth.of(bits.size)

	/** Is automatically set to ´true´ if all [Bit]s are [Bit.False]. */
	private val zero: Boolean = bits.all { it == Bit.False }

	/** Is automatically set to ´true´ if any [Bit]s is [Bit.Undefined]. */
	private val undefined: Boolean = bits.any { it == Bit.Undefined }

	private val error: Boolean = bits.any { it == Bit.Error }

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
		fun undefined(bitWidth: BitWidth): Word = UNDEFINED[bitWidth]!!

		/** Returns a [Word] of the specified [BitWidth] with all [Bit]s in error state. */
		fun error(bitWidth: BitWidth): Word = ERROR[bitWidth]!!

		/** Returns a [Word] of the specified [BitWidth] with all [Bit]s in `false` state. */
		fun falseValue(bitWidth: BitWidth): Word = FALSE[bitWidth]!!

		/** Returns a [Word] of the specified [BitWidth] with all [Bit]s in `true` state. */
		fun trueValue(bitWidth: BitWidth): Word = TRUE[bitWidth]!!

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
		return if (isPartiallyUndefined) {
			binaryString
		} else {
			hexString
		}
	}

	/** ---- [DigitalSignal] interface */

	override val hexString: String by lazy {
		val sb = StringBuilder()
		for (i in max(0, bits.size / 4 - 1) downTo 0) {
			sb.append(nibbleToHexChar(i))
		}
		sb.toString().padStart(bitWidth.width / 4, '0')
	}

	override val isFullyUndefined: Boolean get() = isAllOf(Bit.Undefined)

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

	override val color: CompositeColor by lazy calcColor@ {
		if (bitWidth == BitWidth.BW_1) {
			return@calcColor bitAt(0).color
		}
		if (zero) {
			return@calcColor Themes.get<AntaresTheme>().wordZero
		}
		if (error) {
			return@calcColor Themes.get<AntaresTheme>().error
		}
		if (isFullyUndefined) {
			return@calcColor Themes.get<AntaresTheme>().undefined
		}
		return@calcColor Themes.get<AntaresTheme>().word
	}

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

	/** ---- [Word] */

	override fun getValue(): ULong = longValue!!

	override fun isAllOf(bit: Bit): Boolean = bits.all { it == bit }

	/**
	 * Creates a copy of this [Word] and sets the specified [Bit] in the copy.
	 * @param index the index of the [Bit] to set.
	 * @param bit the [Bit] to set at index [index].
	 * @return the copies and adjusted [Word].
	 */
	override fun withBit(index: Int, bit: Bit): DigitalSignal {
		val bits = mutableListOf<Bit>()
		bits.addAll(this.bits)
		bits[index] = bit
		return Word(bits)
	}

	override fun getSubword(subwordWidth: BitWidth, index: Int): Word {
		// Use the same loop structure as in getSubWordValue
		val subword = mutableListOf<Bit>()
		val digit = min(bitWidth.width, subwordWidth.width) - 1
		for (i in index * subwordWidth.width + digit downTo index * subwordWidth.width) {
			subword.add(0, bits[i])
		}
		return Word(subword)
	}

	override fun getSubwordValue(subwordWidth: BitWidth, index: Int): ULong? {
		var sum: ULong = 0UL
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

	/**
	 * Creates a copy of this [Word] and sets the specified sub-word in the copy.
	 * Note that this method works only for [DigitalSignalRepresentation]s with fully aligned nibbles.
	 * In particular, it doesn't work with [DigitalSignalRepresentation.DECIMAL].
	 *
	 * @param subword the [Word] to be set in the copy of this [Word]
	 * @param index the index of the replaced sub-word. For example, an 8-Bit word consists of
	 * two sub-words with index 0 (bits 0..3) and index 1 (bits 4..7)
	 */
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

	/**
	 * Creates a copy of this [Word] and used all [Bit]s of [subword] where the corresponding
	 * [Bit] in this [Word] is [Bit.Undefined].
	 */
	override fun defineSubword(subword: DigitalSignal, index: Int): Word {
		val resultBits = mutableListOf<Bit>()
		resultBits.addAll(this.bits)
		replaceFromSubword(resultBits, subword as Word, index) { resultBits[it] == Bit.Undefined }
		return Word(resultBits)
	}

	/**
	 * Creates a copy of this [Word]'s value and expands or reduces it to the specified [BitWidth].
	 * Expanding is done by adding zeros to the left, while reducing is done by truncating high order numbers.
	 */
	override fun ofWidth(bitWidth: BitWidth): DigitalSignal = of(bitWidth, getValue())

	override fun shiftLeft(bitCount: Int): DigitalSignal {
		val newBits = bits.toMutableList()
		newBits.add(0, Bit.False)
		newBits.removeLast()
		return Word(newBits)
	}

	override fun shiftRight(bitCount: Int): DigitalSignal {
		val newBits = bits.toMutableList()
		newBits.removeFirst()
		newBits.add(Bit.False)
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
		if (this.bitWidth != other?.bitWidth) {
			return false
		}
		bits.forEachIndexed { index, bit ->
			if (!bit.isConsistentWith(other.bitAt(index))) {
				return false
			}
		}
		return true
	}

	override fun add(other: DigitalSignal): DigitalSignal {
		val sum = if (longValue == null || other.toLong() == null) {
			throw IllegalArgumentException("cannot add non fully defined digital signal")
		} else {
			longValue!! + other.toLong()!!
		}
		return of(bitWidth.max(other.bitWidth), sum)
	}

	override fun add(other: UInt): DigitalSignal {
		val sum = if (longValue == null) {
			throw IllegalArgumentException("cannot add non fully defined digital signal")
		} else {
			longValue!! + other
		}
		return of(bitWidth, sum)
	}

	override fun mod(other: DigitalSignal): DigitalSignal {
		val mod = if (longValue == null || other.toLong() == null) {
			throw IllegalArgumentException("cannot divide non fully defined digital signal")
		} else {
			longValue!!.mod(other.toLong()!!)
		}
		return of(bitWidth.max(other.bitWidth), mod)
	}

	override fun mod(value: ULong): DigitalSignal {
		val mod = if (longValue == null) {
			throw IllegalArgumentException("cannot divide fully defined digital signal")
		} else {
			longValue!!.mod(value)
		}
		return of(bitWidth, mod)
	}
}