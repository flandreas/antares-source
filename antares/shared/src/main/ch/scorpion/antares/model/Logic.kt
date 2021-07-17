package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.jabbah.base.EnumProperty

/**
 * Represents the type of logic of [DigitalPort]s.
 */
enum class Logic(override val customName: String) : EnumProperty<Logic> {

	POSITIVE("positive"),
	NEGATIVE("negative");

    companion object {

	    fun negated(b: Boolean): Logic = if (b) NEGATIVE else POSITIVE

        fun withName(customName: String): Logic {
            for (logic in values()) {
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

	fun evaluate(bit: Bit): Bit {
		return when (this) {
			POSITIVE -> bit
			NEGATIVE -> bit.not()
		}
	}
}