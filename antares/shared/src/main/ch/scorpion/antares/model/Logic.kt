package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException

/**
 * Represents the type of logic of digital [Port]s.
 */
enum class Logic(val customName: String) {
    POSITIVE("positive"), NEGATIVE("negative");

    companion object {
        fun withName(customName: String): Logic {
            for (logic in Logic.values()) {
                if (logic.customName == customName) {
                    return logic
                }
            }
            throw IllegalArgumentException("unknown Logic $customName")
        }
    }

    override fun toString(): String {
        return when (this) {
            POSITIVE -> Translations.getString("element.property.logic.positive")
            NEGATIVE -> Translations.getString("element.property.logic.negative")
        }
    }

    fun evaluate(bit: Boolean): Boolean {
        return when (this) {
            POSITIVE -> bit
            NEGATIVE -> !bit
        }
    }
}