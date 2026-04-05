package io.antarescircuit.jabbah.draw

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.PropertiesProxy
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.Font
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.base.module.BaseModule

/**
 * Extends [Properties] by accessor methods for types that are not available in [io.antarescircuit.jabbah.base].
 */
class DrawProperties(
	target: Properties = BaseModule.properties
) : PropertiesProxy(target) {

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
}