package io.antarescircuit.antares.model.analog

import io.antarescircuit.jabbah.base.EnumProperty
import io.antarescircuit.jabbah.base.Translations

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