package ch.scorpion.jabbah.draw.drawable

import kotlin.math.ceil

/**
 * The resolution of a raster image in "dots per inch".
 */
class Resolution(
    val dpi: Int,
    val name: String = "$dpi dpi"
) {
    companion object {
        val DPI_72 = Resolution(72)
        val DPI_96 = Resolution(96)
        val DPI_150 = Resolution(150)
        val DPI_200 = Resolution(200)
        val DPI_300 = Resolution(300)

        val PREDEFINED = listOf(DPI_72, DPI_96, DPI_150, DPI_200, DPI_300)

        /** The number of millimeter per one inch.*/
        const val MM_PER_INCH = 25.4f

        fun predefinedWithName(name: String): Resolution =
            PREDEFINED.firstOrNull { it.name == name }
                ?: throw IllegalArgumentException("Resolution '$name' not supported")
    }

    override fun toString(): String = name

    fun millimeterToPixel(mm: Int): Int = ceil(mm / MM_PER_INCH * dpi).toInt()
}