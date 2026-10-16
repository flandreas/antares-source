package io.antarescircuit.jabbah.edit.properties.magnitude

import io.antarescircuit.jabbah.base.Translations

enum class SIUnit(
    val customName: String,
    val symbol: String,
    val minimumMagnitude: Magnitude,
    val defaultMagnitude: Magnitude,
    val allowNegative: Boolean = false
) {
    Factor("Factor", "x", Magnitude.One, Magnitude.One),
    Ohm("Ohm", "Ω", Magnitude.One, Magnitude.One),
    Farad("Farad", "F", Magnitude.Pico, Magnitude.Micro),
    Henry("Henry", "H", Magnitude.Nano, Magnitude.One),
    Volt("Volt", "V", Magnitude.Micro, Magnitude.One, allowNegative = true),
    Ampere("Ampère", "A", Magnitude.Micro, Magnitude.Milli, allowNegative = true),
    Second("Second", "s", Magnitude.Nano, Magnitude.Nano),
    Hertz("Hertz", "Hz", Magnitude.One, Magnitude.One);

    companion object {

        fun withCustomName(name: String): SIUnit =
            entries.firstOrNull { it.customName == name }
                ?: throw IllegalArgumentException(Translations.getString("edit.magnitude.invalidUnit.text", name))
    }
}
