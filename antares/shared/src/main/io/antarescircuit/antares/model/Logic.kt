package io.antarescircuit.antares.model

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.base.EnumProperty

/**
 * Represents the type of logic of [DigitalPort]s.
 */
enum class Logic(override val customName: String) : EnumProperty<Logic> {

	/** A [DigitalPort] is active with [Bit.True]. */
	POSITIVE("positive"),

	/** A [DigitalPort] is active with [Bit.False] .*/
	NEGATIVE("negative");

    companion object {

	    const val BASE_KEY = "element.property.logic"

	    fun negated(b: Boolean): Logic = if (b) NEGATIVE else POSITIVE

        fun withName(customName: String): Logic {
            for (logic in entries) {
                if (logic.customName == customName) {
                    return logic
                }
            }
            throw IllegalArgumentException("unknown Logic $customName")
        }
    }

    override fun toString(): String =
        when (this) {
            POSITIVE -> Translations.getString("$BASE_KEY.positive")
            NEGATIVE -> Translations.getString("$BASE_KEY.negative")
        }

    fun evaluate(bit: Boolean): Boolean =
        when (this) {
            POSITIVE -> bit
            NEGATIVE -> !bit
        }

	fun evaluate(bit: Bit): Bit =
        when (this) {
            POSITIVE -> bit
            NEGATIVE -> bit.not()
        }

	fun evaluate(signal: DigitalSignal): DigitalSignal =
        when (this) {
            POSITIVE -> signal
            NEGATIVE -> signal.not()
        }
}