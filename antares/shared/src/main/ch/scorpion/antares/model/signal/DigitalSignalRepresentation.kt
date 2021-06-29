package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations


enum class DigitalSignalRepresentation(override val customName: String) : EnumProperty<DigitalSignalRepresentation> {

    BINARY("binary") {
	    override val prefix: String get() = "0b"
	    override val base: Int get() = 2
	    override val suffix: String get() = "b"
        override fun bits(): Int = 1
        override fun represent(signal: DigitalSignal): String = signal.toBinaryString()
        override fun signalAt(signal: DigitalSignal, index: Int): DigitalSignal = signal.getSubword(BitWidth.BW_1, index)
        override fun digitToWord(bitWidth: BitWidth, digit: Char): Word? = BitOperation.binaryDigitToWord(digit)
    },

    HEXADECIMAL("hex") {
	    override val prefix: String get() = "0x"
	    override val base: Int get() = 16
	    override val suffix: String get() = "h"
        override fun bits(): Int = 4
        override fun represent(signal: DigitalSignal): String = signal.toHexString()
        override fun signalAt(signal: DigitalSignal, index: Int): DigitalSignal = signal.getSubword(BitWidth.BW_4, index)
        override fun digitToWord(bitWidth: BitWidth, digit: Char): Word? = BitOperation.hexDigitToWord(bitWidth, digit)
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
    abstract fun bits(): Int

    /** Represents the specified [DigitalSignal] as a [String].*/
    abstract fun represent(signal: DigitalSignal): String

    /** Returns the sub-signal of a [DigitalSignal] at the specified index.*/
    abstract fun signalAt(signal: DigitalSignal, index: Int): DigitalSignal

    /** Converts a single digit to the [Word] with the corresponding length. */
    abstract fun digitToWord(bitWidth: BitWidth, digit: Char): Word?

    override fun toString(): String {
        return when (this) {
            BINARY -> Translations.getString("element.property.DigitalSignalRepresentation.binary")
            HEXADECIMAL -> Translations.getString("element.property.DigitalSignalRepresentation.hexadecimal")
        }
    }
}