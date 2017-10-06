package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Extends [Properties] by accessor methods for types that are not available in [ch.scorpion.jabbah.base].
 */
class DrawProperties(
        private val target: Properties = BaseModule.properties
) : Properties() {

    fun getFont(name: String): Font {
        return target.get(name)
    }

    fun getColor(name: String): Color {
        return target.get(name)
    }

    fun getOptionalColor(name: String): Color? {
        return target.getOptional(name)
    }

    fun getStroke(name: String): Stroke {
        return target.get(name)
    }

    override fun <T> getOptional(name: String): T? {
        return target.getOptional(name)
    }

    override fun set(name: String, value: Any) {
        target.set(name, value)
    }
}