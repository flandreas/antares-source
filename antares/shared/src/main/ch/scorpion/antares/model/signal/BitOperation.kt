package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_0
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_1
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_2
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_3
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_4
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_5
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_6
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_7
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_8
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_9
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_A
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_B
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_C
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_D
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_E
import ch.scorpion.jabbah.base.event.KeyEvent.Companion.VK_F
import kotlin.math.max
import kotlin.math.pow

/**
 * Utility functions for working with bits.
 */
object BitOperation {

    val HEX_CHAR = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'X', 'Z')
	val HEY_KEY = listOf(VK_0, VK_1, VK_2, VK_3, VK_4, VK_5, VK_6, VK_7, VK_8, VK_9, VK_A, VK_B, VK_C, VK_D, VK_E, VK_F)
	private val POWER = Array(63 + 1) {
		2.0.pow(it).toULong()
	}

	fun getBitAt(value: ULong, index: Int): Boolean {
        return value shr index and 1UL == 1UL
    }

    fun setBitAt(value: ULong, index: Int): ULong {
        return value or (1UL shl index)
    }

	fun setBitAt(value: ULong, bit: Int, index: Int): ULong {
		return if (bit.mod(2) == 0) {
			clearBitAt(value, index)
		} else {
			setBitAt(value, index)
		}
	}

    fun clearBitAt(value: ULong, index: Int): ULong {
        return value and (1UL shl index).inv()
    }

    fun power(value: Byte): ULong {
	    checkArgument(value <= 63, "value must not be larger than 64")
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
			if (value[0] !in '0'..bitWidth.maxValue.toInt().toChar()) {
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
    fun hexToLong(hex: String): ULong {
        var value = 0UL
        var factor = 1UL

        for (c in hex.uppercase().reversed()) {
	        value += when (c) {
		        in '0'..'9' -> factor * (c.code - '0'.code).toULong()
		        in 'A'..'F' -> factor * (c.code - 'A'.code + 10).toULong()
		        else -> throw IllegalArgumentException("'$hex' is not a valid hexadecimal number")
	        }
            factor *= 16UL
        }

        return value
    }

    /**
     * Returns the specified hexadecimal character as a [DigitalSignal] of the specified [BitWidth], or `null` if [hex]
     * contains a non-hexadecimal character.
     */
    fun hexDigitToWord(bitWidth: BitWidth, hex: Char): DigitalSignal? {
	    val uppercaseHex = hex.uppercaseChar()
        if (!HEX_CHAR.contains(uppercaseHex)) {
            return null
        }
	    if (uppercaseHex == Bit.ALL_UNDEFINED_CHAR) {
	    	return DigitalSignalFactory.allOf(bitWidth, Bit.Undefined)
	    }
	    if (uppercaseHex == Bit.ERROR_CHAR) {
	    	return DigitalSignalFactory.allOf(bitWidth, Bit.Error)
	    }
        val value = hexToLong(hex.toString())
	    if (value <= bitWidth.maxValue) {
		    return DigitalSignalFactory.of(bitWidth, value)
	    }
	    // Overflow
	    return null
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
				val value = decimal.code.toULong() - '0'.code.toULong()
				if (value <= bitWidth.maxValue) {
					return DigitalSignalFactory.of(bitWidth, value)
				}
				return null
			}
		}
	}

    /**
     * Converts a decimal number into its hexadecimal string representation.
     * Standard Long.toString(radix) not supported on JS platform.
     */
    fun longToHex(value: ULong): String {
        if (value == 0UL) {
            return "0"
        }
        val hex = StringBuilder()
        var num = value
        while (num > 0UL) {
            hex.append(HEX_CHAR[(num % 16UL).toInt()])
            num /= 16UL
        }
        return hex.toString().reversed()
    }

	fun longToHexPadded(value: ULong, bitWidth: BitWidth): String {
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