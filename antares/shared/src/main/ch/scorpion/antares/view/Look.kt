package ch.scorpion.antares.view

import ch.scorpion.jabbah.draw.graphics.FontFamily
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.FontStyle

object Look {

    val SCALE: Int = 7
    val GRID: Int = 1 * SCALE

    val UI_FONT = FontImpl(FontFamily.DIALOG, FontStyle.PLAIN.value, 11)
    val INT_PIN_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (1.8 * SCALE).toInt())
    val EXT_PIN_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (1.5 * SCALE).toInt())
}