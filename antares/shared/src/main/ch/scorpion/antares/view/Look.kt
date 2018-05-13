package ch.scorpion.antares.view

import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.draw.graphics.FontFamily
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.FontStyle

object Look {

    const val SCALE: Int = 7
    const val GRID: Int = 1 * SCALE

    val UI_FONT = FontImpl(FontFamily.DIALOG, FontStyle.PLAIN.value, 11)
    val INT_PIN_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (1.8 * SCALE).toInt())
    val EXT_PIN_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (1.5 * SCALE).toInt())
    val ANNOTATION_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (1.4 * SCALE).toInt())
    val ADDRESSABLE_CONTENTS_FONT = FontImpl(FontFamily.MONOSPACED, FontStyle.PLAIN.value, (1.8 * SCALE).toInt())

    fun scaleToGrid(value: Int): Int {
        return GRID * Math.ceil(value.toDouble() / GRID).toInt()
    }

    fun scaleToDoubleGrid(value: Int): Int {
        return 2 * GRID * Math.ceil(value.toDouble() / 2 / GRID).toInt()
    }
}