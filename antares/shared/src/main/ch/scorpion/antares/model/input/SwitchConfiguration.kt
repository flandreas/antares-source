package ch.scorpion.antares.model.input

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations

enum class SwitchConfiguration(
    override val customName: String,
    val portCount: Int
) : EnumProperty<SwitchConfiguration> {

    SPST("spst", 2),
    SPDT("spdt", 3);

    companion object {
        const val BASE_KEY = "element.property.switchConfiguration"

        fun withName(customName: String): SwitchConfiguration =
            entries.firstOrNull { it.customName == customName }
                ?: throw IllegalArgumentException("unknown SwitchConfiguration $customName")
    }

    override fun toString(): String {
        return when (this) {
            SPST -> Translations.getString("element.property.switchConfiguration.spst")
            SPDT -> Translations.getString("element.property.switchConfiguration.spdt")
        }
    }
}