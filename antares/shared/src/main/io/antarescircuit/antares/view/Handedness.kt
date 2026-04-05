package io.antarescircuit.antares.view

import io.antarescircuit.jabbah.base.EnumProperty
import io.antarescircuit.jabbah.base.Translations

/**
 * Represents the right/left handedness.
 */
enum class Handedness(override val customName: String) : EnumProperty<Handedness> {

    RIGHT("right"),
    LEFT("left");

    companion object {

	    const val BASE_KEY = "element.property.TriStateBuffer.handedness"

        fun withName(customName: String): Handedness {
            for (handedness in values()) {
                if (handedness.customName == customName) {
                    return handedness
                }
            }
            throw IllegalArgumentException("unknown Handedness $customName")
        }
    }

    override fun toString(): String {
        return when(this) {
            RIGHT -> Translations.getString("element.property.handedness.right")
            LEFT -> Translations.getString("element.property.handedness.left")
        }
    }
}