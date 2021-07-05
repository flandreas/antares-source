package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations
import kotlin.math.max


enum class DigitalSignalRepresentation(override val customName: String) : EnumProperty<DigitalSignalRepresentation> {

    BINARY("binary") {
	    override val prefix: String get() = "0b"
	    override val base: Int get() = 2
	    override val suffix: String get() = "b"
        override val bitCount: Int get() = 1
	    override val digitGroupSize: Int get() = 4
	    override fun digitCount(bitWidth: BitWidth): Int = bitWidth.width
        override fun represent(signal: DigitalSignal): String = signal.toBinaryString()
	    override fun signalAt(signal: DigitalSignal, index: Int): DigitalSignal = signal.getSubword(BitWidth.of(bitCount), index)
        override fun digitToWord(bitWidth: BitWidth, digit: Char): DigitalSignal? = BitOperation.binaryDigitToWord(digit)
	    override fun withDigit(word: DigitalSignal, digitWord: DigitalSignal, index: Int): DigitalSignal = word.withSubwordValue(digitWord, index)
    },

	DECIMAL("decimal") {
		override val prefix: String get() = "0d"
		override val base: Int get() = 10
		override val suffix: String get() = "d"
		override val bitCount: Int get() =  4
		override val digitGroupSize: Int get() = 3
		override fun digitCount(bitWidth: BitWidth): Int = bitWidth.power().toString().length
		override fun signalAt(signal: DigitalSignal, index: Int): DigitalSignal {
			val bitWidth = if (signal.bitWidth == BitWidth.BW_1) BitWidth.BW_1 else BitWidth.of(bitCount)
			val value = signal.toLong()
				?: return if (signal.isPartiallyUndefined) {
					DigitalSignalFactory.undefined(bitWidth)
				} else {
					DigitalSignalFactory.error(bitWidth)
				}
			val s = value.toString().padStart(index + 1, '0')
			return DigitalSignalFactory.of(bitWidth, (s[s.length - 1 - index].code - '0'.code).toLong())

		}
		override fun represent(signal: DigitalSignal): String = signal.toDecimalString()
		override fun digitToWord(bitWidth: BitWidth, digit: Char): DigitalSignal? = BitOperation.decimalDigitToWord(bitWidth, digit)
		override fun withDigit(word: DigitalSignal, digitWord: DigitalSignal, index: Int): DigitalSignal {
			var s = word.getValue().toString().padStart(index + 1, '0')
			val sIndex = s.length - 1 - index
			s = StringBuilder(s).also { it[sIndex] = digitWord.getValue().toString()[0] }.toString()
			val value = s.toLong()
			if (value < word.bitWidth.power()) {
				return DigitalSignalFactory.of(word.bitWidth, value)
			}
			return word
		}
	},

    HEXADECIMAL("hex") {
	    override val prefix: String get() = "0x"
	    override val base: Int get() = 16
	    override val suffix: String get() = "h"
	    override val bitCount: Int get() = 4
	    override val digitGroupSize: Int get() = 4
	    override fun digitCount(bitWidth: BitWidth): Int = max(1, bitWidth.width / bitCount)
	    override fun signalAt(signal: DigitalSignal, index: Int): DigitalSignal = signal.getSubword(BitWidth.of(bitCount), index)
        override fun represent(signal: DigitalSignal): String = signal.toHexString()
        override fun digitToWord(bitWidth: BitWidth, digit: Char): DigitalSignal? = BitOperation.hexDigitToWord(bitWidth, digit)
	    override fun withDigit(word: DigitalSignal, digitWord: DigitalSignal, index: Int): DigitalSignal = word.withSubwordValue(digitWord, index)
    };

    companion object {
        fun withName(customName: String): DigitalSignalRepresentation {
            for (r in values()) {
                if (r.customName == customName) {
                    return r
                }
            }
            throw IllegalArgumentException("unknown DigitalSignalRepresentation $customName")
        }
    }

	abstract val prefix: String

	abstract val base: Int

	abstract val suffix: String

    /** Returns the number of [Bit]s needed to represent a single digit.*/
    abstract val bitCount: Int

    abstract val digitGroupSize: Int

    /** Returns the number of digits required to represent a signal of [bitWidth].*/
    abstract fun digitCount(bitWidth: BitWidth): Int

    /** Represents the specified [DigitalSignal] as a [String].*/
    abstract fun represent(signal: DigitalSignal): String

    /** Returns the sub-signal of a [DigitalSignal] at the specified digit index, where the least significant index is 0*/
	abstract fun signalAt(signal: DigitalSignal, index: Int): DigitalSignal

    /**
     * Converts a single digit to the [DigitalSignal] with the corresponding length, or `null` if [digit] can't be represented
     * with the number of [Bit] available with [bitWidth], or if [digit] is an illegal character.
     */
    abstract fun digitToWord(bitWidth: BitWidth, digit: Char): DigitalSignal?

	/**
	 * Creates a copy of [word] and sets the specified [digitWord] in the copy.
	 * TODO: This should rather be 'withDigit(word: Word, digit: Int, index: Int): Word'
	 *
	 * @param word the [DigitalSignal] to be changed
	 * @param digitWord the [DigitalSignal] to be set in the copy of this [DigitalSignal]
	 * @param index the index of the replaced sub-word. For example, an 8-Bit word consists of
	 * two sub-words with index 0 (bits 0..3) and index 1 (bits 4..7)
	 */
	abstract fun withDigit(word: DigitalSignal, digitWord: DigitalSignal, index: Int): DigitalSignal

    override fun toString(): String {
        return when (this) {
            BINARY -> Translations.getString("element.property.DigitalSignalRepresentation.binary")
            DECIMAL -> Translations.getString("element.property.DigitalSignalRepresentation.decimal")
            HEXADECIMAL -> Translations.getString("element.property.DigitalSignalRepresentation.hexadecimal")
        }
    }
}