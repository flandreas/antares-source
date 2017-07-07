package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.exception.NoSuchElementException

/**
 * Extends [Properties] by accessor methods for types that are not available in [ch.scorpion.jabbah.base].
 */
class DrawProperties : Properties() {

    fun getFont(name: String, defaultValue: Font? = null): Font {
        return get(name, defaultValue)
    }

    fun getColor(name: String, defaultValue: Color? = null): Color {
        return get(name, defaultValue)
    }

    fun getOptionalColor(name: String): Color? {
        return getOptional(name)
    }

    fun getStroke(name: String, defaultValue: Stroke? = null): Stroke {
        return get(name, defaultValue)
    }
}