package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations

enum class AnalogOscilloscopeSignalType(override val customName: String) : EnumProperty<AnalogOscilloscopeSignalType> {

    Voltage("voltage"),
    Current("current");

    companion object {
        const val BASE_KEY = "element.property.analogOscilloscopeSignalType"

        fun withName(customName: String): AnalogOscilloscopeSignalType =
            entries.firstOrNull { it.customName == customName }
                ?: throw IllegalArgumentException("unknown signalType $customName")
    }

    override fun toString(): String =
        when (this) {
            Voltage -> Translations.getString("$BASE_KEY.voltage")
            Current -> Translations.getString("$BASE_KEY.current")
        }
}