package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.exception.IllegalArgumentException

/**
 * Utility functions for working with bits.
 */
object BitOperation {

    val BINARY = listOf('0', '1')
    val HEX = listOf('0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F')

    fun getBitAt(value: Long, index: Int): Boolean {
        return value shr index and 1 == 1L
    }

    fun setBitAt(value: Long, index: Int): Long {
        return value or (1L shl index)
    }

    fun clearBitAt(value: Long, index: Int): Long {
        return value and (1L shl index).inv()
    }

    fun power(value: Long): Int {
        // TODO Use Hashmap for values between 0 and 31.
        var sum = 1
        for (i in 0..value - 1) {
            sum *= 2
        }
        return sum
    }

    /** Converts a hexadecimal value to a decimal long value*/
    fun hexToLong(hex: String): Long {
        var value = 0L
        var factor = 1L

        for (c in hex.toUpperCase().reversed()) {
            if (c in '0'..'9') {
                value += factor * (c.toInt() - '0'.toInt())
            } else if (c in 'A'..'F') {
                value += factor * (c.toInt() - 'A'.toInt() + 10)
            } else {
                throw IllegalArgumentException("'$hex' is not a valid hexadecimal number")
            }
            factor *= 16
        }

        return value
    }

    /**
     * Returns the specified hexadecimal character as a [Word] of the specified [BitWidth], or `null` if [hex]
     * contains a non-hexadecimal character.
     */
    fun hexDigitToWord(bitWidth: BitWidth, hex: Char): Word? {
        if (!HEX.contains(hex.toUpperCase())) {
            return null
        }
        val value = hexToLong(hex.toString())
        if (value >= bitWidth.power()) {
            // Overflow
            return null
        }
        return Word.of(bitWidth, value)
    }

    /**
     * Returns the specified binary character as a [Word] of [BitWidth.BW_1], or `null` if [binary]
     * contains a non-binary character (0, 1).
     */
    fun binaryDigitToWord(binary: Char): Word? {
        if (!BINARY.contains(binary)) {
            return null
        }
        return Word.of(binary == '1')
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

    /**
     * Converts a decimal value in the range 0..15 to the corresponding hexadecimal digit.
     * @Deprecated use [longToHex]
     */
    fun hexDigit(value: Long): Char {
        if (value in 0..9) {
            return ('0'.toInt() + value).toChar()
        }
        if (value in 10..15) {
            return ('A'.toInt() + (value - 10)).toChar()
        }
        throw IllegalArgumentException("value must be between 0 and 15")
    }
}