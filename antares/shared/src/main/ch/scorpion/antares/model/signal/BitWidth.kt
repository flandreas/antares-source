package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.EnumProperty

/**
 * Defines the supported [DigitalSignal] widths.
 */
enum class BitWidth(val width: Int, val size: String) : EnumProperty<BitWidth> {
    BW_1(1, "1"),
    BW_2(2, "4"),
    BW_4(4, "16"),
    BW_8(8, "256"),
	BW_12(12, "4K"),
    BW_16(16, "64K"),
	BW_20(20, "1M"),
	BW_24(24, "16M"),
	BW_28(28, "256M"),
    BW_32(32, "4G");

    companion object {

        fun of(width: Int): BitWidth {
            for (bw in values()) {
                if (bw.width == width) {
                    return bw
                }
            }
            throw IllegalArgumentException("Unsupported BitWidth of '$width'")
        }

        fun withName(customName: String): BitWidth {
            for (bw in values()) {
                if (bw.customName == customName) {
                    return bw
                }
            }
            throw IllegalArgumentException("Unknown BitWidth '$customName'")
        }
    }

    override val customName: String get() = width.toString()

    fun power(): Long = BitOperation.power(width.toByte())

    override fun toString(): String = customName
}
