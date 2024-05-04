package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.EnumProperty

/**
 * Represents the type of enter key behavior of [Keyboard]s.
 */
enum class EnterBehavior(override val customName: String) : EnumProperty<EnterBehavior> {

	/** Sends \r. */
	CR("cr"),

	/** Sends \n .*/
	LF("lf");

    companion object {
	    const val BASE_KEY = "element.property.enterBehavior"

        fun withName(customName: String): EnterBehavior {
            for (code in values()) {
                if (code.customName == customName) {
                    return code
                }
            }
            throw IllegalArgumentException("unknown EnterBehavior $customName")
        }
    }

    override fun toString(): String {
        return when (this) {
            CR -> Translations.getString("element.property.enterBehavior.cr")
            LF -> Translations.getString("element.property.enterBehavior.lf")
        }
    }
}