package ch.scorpion.antares.view

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException

/**
 * Represents the right/left handedness.
 */
enum class Handedness(val customName: String){

    RIGHT("right"),
    LEFT("left");

    companion object {
        fun withName(customName: String): Handedness {
            for (handedness in Handedness.values()) {
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