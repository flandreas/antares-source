package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException

/**
 * Represents the supported types of [InputPort] triggering.
 */
enum class Trigger(val customName: String) {
    LEVEL("level"), EDGE("edge");

    companion object {
        fun withName(customName: String): Trigger {
            for (trigger in Trigger.values()) {
                if (trigger.customName == customName) {
                    return trigger
                }
            }
            throw IllegalArgumentException("unknown Trigger $customName")
        }
    }

    override fun toString(): String {
        return when(this) {
            LEVEL -> Translations.getString("element.property.trigger.level")
            EDGE -> Translations.getString("element.property.trigger.edge")
        }
    }
}