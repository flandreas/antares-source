package ch.scorpion.jabbah.graphics

import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.RasterImage

class TestRasterImage(override val width: Int, override val height: Int) : RasterImage {

    private val data = mutableMapOf<Pair<Int, Int>, Color>()

    override fun setColor(x: Int, y: Int, color: Color) {
        data[x to y] = color
    }

    fun getColor(x: Int, y: Int): Color? = data[x to y]
}