package io.antarescircuit.jabbah.edit.properties.magnitude

import io.antarescircuit.jabbah.base.Translations

object MagnitudeValueParser {

    fun parse(text: String, unit: SIUnit, defaultMagnitude: Magnitude = unit.defaultMagnitude): MagnitudeValue {
        val s = text.trim()

        // Try parsing as pure number, without magnitude and unit, thereby applying default magnitude
        try {
            val value = s.toDouble()
            if (value <= 0) {
                throwInvalidValueException(text)
            }
            return MagnitudeValue(value, defaultMagnitude, unit)
        } catch (_: NumberFormatException) {
            // continue
        }

        // Try parsing with magnitude
        return MagnitudeValue(extractValue(s), extractMagnitude(s, unit), unit)
    }

    fun parseWithUnits(text: String, vararg units: SIUnit): MagnitudeValue {
        require(units.isNotEmpty()) { "At least one unit has to be specified." }
        var exception: IllegalArgumentException? = null
        for (unit in units) {
            try {
                return parse(text, unit)
            } catch (e: IllegalArgumentException) {
                exception = e
            }
        }
        throw exception!!
    }

    private fun extractValue(text: String): Double {
        val valueText = text.dropLastWhile { !it.isDigit() }.trim()
        try {
            val value = valueText.toDouble()
            if (value <= 0) {
                throwInvalidValueException(valueText)
            }
            return value
        } catch (_: NumberFormatException) {
            throwInvalidValueException(valueText)
        }
    }

    private fun extractMagnitude(text: String, unit: SIUnit): Magnitude {
        val magnitudeText = text.dropWhile { it.isDigit() || it == '.' || it == '.' }.trim()

        val magnitude = Magnitude.entries.firstOrNull { it.matchText(magnitudeText) }
        if (magnitude != null) {
            return magnitude
        }

        // Check if the magnitudeText ends with the unit symbol
        if (magnitudeText.endsWith(unit.symbol, ignoreCase = true)) {
            val magnitudeWithoutUnit = magnitudeText.substring(0, magnitudeText.length - unit.symbol.length)
            return Magnitude.entries.firstOrNull { it.matchText(magnitudeWithoutUnit) }
                ?: throwInvalidUnitException(magnitudeText)
        }

        throwInvalidUnitException(magnitudeText)
    }

    private fun throwInvalidValueException(valueText: String): Nothing =
        throw IllegalArgumentException(Translations.getString("edit.magnitude.invalidValue.text", valueText))

    private fun throwInvalidUnitException(unitText: String): Nothing =
        throw IllegalArgumentException(Translations.getString("edit.magnitude.invalidUnit.text", unitText))
}