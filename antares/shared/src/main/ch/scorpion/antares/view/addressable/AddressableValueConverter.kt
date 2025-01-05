package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.base.Translations

/**
 * Converts [Long] values stored in [Addressable]s to a displayable
 * [String] value, typically depending on a current [DigitalSignalRepresentation],
 * such as hexadecimal or binary.
 */
enum class AddressableValueConverter {

    Hexadecimal {
        override fun render(value: ULong, bitWidth: BitWidth): String =
            BitOperation.longToHexPadded(value, bitWidth)

        override fun parse(value: String, bitWidth: BitWidth): ULong? =
            BitOperation.normalizeHex(value.trim(), bitWidth)?.let {
                try {
                    BitOperation.hexToLong(it)
                } catch (e: NumberFormatException) {
                    null
                }
            }
    },

    Decimal {
        override fun render(value: ULong, bitWidth: BitWidth): String = value.toString()

        override fun parse(value: String, bitWidth: BitWidth): ULong? {
            return try {
                notExceedingMax(value.toULong(), bitWidth)
            } catch (e: NumberFormatException) {
                null
            }
        }
    },

    Octal {
        override fun render(value: ULong, bitWidth: BitWidth): String =
            BitOperation.longToOctal(value)

        override fun parse(value: String, bitWidth: BitWidth): ULong? =
            try {
                notExceedingMax(BitOperation.octalToLong(value), bitWidth)
            } catch (e: NumberFormatException) {
                null
            }
    },

    Binary {
        override fun render(value: ULong, bitWidth: BitWidth): String =
            BitOperation.longToBinaryPadded(value, bitWidth)

        override fun parse(value: String, bitWidth: BitWidth): ULong? =
            try {
                notExceedingMax(BitOperation.binaryToLong(value), bitWidth)
            } catch (e: NumberFormatException) {
                null
            }
    };


    abstract fun render(value: ULong, bitWidth: BitWidth): String
    abstract fun parse(value: String, bitWidth: BitWidth): ULong?

    override fun toString(): String {
        return when (this) {
            Hexadecimal -> Translations.getString("antares.addressableValueConverter.hexadecimal.name")
            Decimal -> Translations.getString("antares.addressableValueConverter.decimal.name")
            Octal -> Translations.getString("antares.addressableValueConverter.octal.name")
            Binary -> Translations.getString("antares.addressableValueConverter.binary.name")
        }
    }

    protected fun notExceedingMax(value: ULong, bitWidth: BitWidth): ULong? {
        return if (value <= bitWidth.maxValue) {
            value
        } else {
            null
        }
    }
}
