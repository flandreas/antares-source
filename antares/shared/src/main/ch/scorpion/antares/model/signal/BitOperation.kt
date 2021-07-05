package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.checkArgument
import kotlin.math.max
import kotlin.math.pow

/**
 * Utility functions for working with bits.
 */
object BitOperation {

    private val HEX = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'X', 'Z')
	private val POWER = Array(32 + 1) { 2.0.pow(it).toLong() }

	fun getBitAt(value: Long, index: Int): Boolean {
        return value shr index and 1 == 1L
    }

    fun setBitAt(value: Long, index: Int): Long {
        return value or (1L shl index)
    }

    fun clearBitAt(value: Long, index: Int): Long {
        return value and (1L shl index).inv()
    }

    fun power(value: Byte): Long {
	    checkArgument(value <= 32, "value must not be larger than 32")
	    return POWER[value.toInt()]
    }

	/**
	 * Returns the normalized representation of a hexadecimal [String] in a particular [BitWidth],
	 * or `null` if the combination is invalid (e.g. hex string is too long, to big, or contains invalid characters).
	 */
	fun normalizeHex(hex: String, bitWidth: BitWidth): String? {
		val value = hex.uppercase()
		if (bitWidth < BitWidth.BW_4) {
			if (value.length > 1) {
				return null
			}
			val maxDigit = bitWidth.power() - 1
			if (value[0] !in '0'..maxDigit.toInt().toChar()) {
				return null
			}
			return value
		} else {
			val length = bitWidth.width / 4
			if (value.length > length) {
				return null
			}
			if (value.firstOrNull { it !in '0'..'9' && it !in 'A'..'F' } != null) {
				return null
			}

			return value.padStart(length, '0')
		}
	}

    /** Converts a hexadecimal value to a decimal long value*/
    fun hexToLong(hex: String): Long {
        var value = 0L
        var factor = 1L

        for (c in hex.uppercase().reversed()) {
	        value += when (c) {
		        in '0'..'9' -> factor * (c.code - '0'.code)
		        in 'A'..'F' -> factor * (c.code - 'A'.code + 10)
		        else -> throw IllegalArgumentException("'$hex' is not a valid hexadecimal number")
	        }
            factor *= 16
        }

        return value
    }

    /**
     * Returns the specified hexadecimal character as a [DigitalSignal] of the specified [BitWidth], or `null` if [hex]
     * contains a non-hexadecimal character.
     */
    fun hexDigitToWord(bitWidth: BitWidth, hex: Char): DigitalSignal? {
	    val uppercaseHex = hex.uppercaseChar()
        if (!HEX.contains(uppercaseHex)) {
            return null
        }
	    if (uppercaseHex == Bit.ALL_UNDEFINED_CHAR) {
	    	return DigitalSignalFactory.allOf(bitWidth, Bit.Undefined)
	    }
	    if (uppercaseHex == Bit.ERROR_CHAR) {
	    	return DigitalSignalFactory.allOf(bitWidth, Bit.Error)
	    }
        val value = hexToLong(hex.toString())
        if (value >= bitWidth.power()) {
            // Overflow
            return null
        }
        return DigitalSignalFactory.of(bitWidth, value)
    }

    /**
     * Returns the specified binary character as a [DigitalSignal] of [BitWidth.BW_1], or `null` if [binary]
     * contains a non-binary character (0, 1).
     */
    fun binaryDigitToWord(binary: Char): DigitalSignal? {
	    return when (binary.uppercaseChar()) {
	    	'0' -> DigitalSignalFactory.of(false)
		    '1' -> DigitalSignalFactory.of(true)
		    Bit.ALL_UNDEFINED_CHAR -> DigitalSignalFactory.of(Bit.Undefined)
		    Bit.ERROR_CHAR -> DigitalSignalFactory.of(Bit.Error)
		    else -> null
	    }
    }

	fun decimalDigitToWord(bitWidth: BitWidth, decimal: Char): DigitalSignal? {
		return when (decimal.uppercaseChar()) {
			Bit.ALL_UNDEFINED_CHAR -> DigitalSignalFactory.allOf(bitWidth, Bit.Undefined)
			Bit.ERROR_CHAR -> DigitalSignalFactory.allOf(bitWidth, Bit.Error)
			else -> {
				val value = decimal.code.toLong() - '0'.code.toLong()
				if (value >= bitWidth.power()) {
					return null
				}
				return DigitalSignalFactory.of(bitWidth, value)
			}
		}
	}

    /**
     * Converts a decimal number into its hexadecimal string representation.
     * Standard Long.toString(radix) not supported on JS platform.
     */
    fun longToHex(value: Long): String {
        if (value == 0L) {
            return "0"
        }
        val hex = StringBuilder()
        var num = value
        while (num > 0) {
            hex.append(HEX[(num % 16).toInt()])
            num /= 16
        }
        return hex.toString().reversed()
    }

	fun longToHexPadded(value: Long, bitWidth: BitWidth): String {
		return longToHex(value).padStart(max(1, bitWidth.width / 4), '0')
	}

    /**
     * Converts a decimal value in the range 0..15 to the corresponding hexadecimal digit.
     * @Deprecated use [longToHex]
     */
    fun hexDigit(value: Long): Char {
        if (value in 0..9) {
            return ('0'.code + value).toInt().toChar()
        }
        if (value in 10..15) {
            return ('A'.code + (value - 10)).toInt().toChar()
        }
        throw IllegalArgumentException("value must be between 0 and 15")
    }
}