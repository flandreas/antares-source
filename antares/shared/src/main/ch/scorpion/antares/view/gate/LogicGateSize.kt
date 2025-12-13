package ch.scorpion.antares.view.gate

import ch.scorpion.jabbah.base.Translations

enum class LogicGateSize(val customName: String, val factor: Float) {
    SMALL("small", 0.5f),
    MEDIUM("medium", 0.7f),
    LARGE("large", 1.0f);

    companion object {

        const val BASE_KEY = "element.property.logicGateSize"

        fun withName(name: String): LogicGateSize =
            LogicGateSize.entries.find { it.customName == name }
                ?: throw IllegalArgumentException("unknown LogicGateSize $name")
    }

    override fun toString(): String =
        when (this) {
            SMALL -> Translations.getString("element.property.logicGateSize.small")
            MEDIUM -> Translations.getString("element.property.logicGateSize.medium")
            LARGE -> Translations.getString("element.property.logicGateSize.large")
        }
}