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

    fun getFont(name: String): Font {
        return get(name)
    }

    fun getColor(name: String): Color {
        return get(name)
    }

    fun getOptionalColor(name: String): Color? {
        return getOptional(name)
    }

    fun getStroke(name: String): Stroke {
        return get(name)
    }
}