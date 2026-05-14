package io.antarescircuit.jabbah.edit.properties.magnitude

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude.*
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue.Companion.DIGIT_COUNT
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.math.*

/**
 * @property value the value as entered by the user, e.g. 100 for "100 K"
 * @property magnitude the extracted [Magnitude], e.g. [Magnitude.Kilo]
 * @property unit the unit of the value, e.g. [SIUnit.Ohm] for "Ohm"
 */
data class MagnitudeValue(
    val value: Double,
    val magnitude: Magnitude,
    val unit: SIUnit
) {

    companion object {

        const val MAGNITUDE_VALUE_EXT = "Value"
        const val MAGNITUDE_NAME_EXT = "Magnitude"
        const val UNIT_NAME_EXT = "Unit"

        private const val DIGIT_COUNT = 3

        fun readWithUnit(name: String, reader: StoreReader): MagnitudeValue =
            MagnitudeValue(
                reader.readDouble("$name$MAGNITUDE_VALUE_EXT"),
                Magnitude.read("$name$MAGNITUDE_NAME_EXT", reader),
                SIUnit.withCustomName(reader.readString("$name$UNIT_NAME_EXT"))
            )

        fun read(name: String, reader: StoreReader, unit: SIUnit): MagnitudeValue =
            MagnitudeValue(
                reader.readDouble("$name$MAGNITUDE_VALUE_EXT"),
                Magnitude.read("$name$MAGNITUDE_NAME_EXT", reader),
                unit
            )
    }

    constructor(value: Long, magnitude: Magnitude, unit: SIUnit) :
        this(value.toDouble(), magnitude, unit)

    init {
        if (magnitude.ordinal < unit.minimumMagnitude.ordinal) {
            throw IllegalArgumentException(Translations.getString("edit.magnitude.tooSmall.text", magnitude.customName, unit.customName))
        }
    }

    fun withBaseValue(value: Double, snap: Boolean = false): MagnitudeValue =
        MagnitudeValue(value, One, unit).normalize(snap)

    fun write(name: String, writer: StoreWriter, writeUnit: Boolean = false) {
        writer.writeDouble("$name$MAGNITUDE_VALUE_EXT", value)
        magnitude.write("$name$MAGNITUDE_NAME_EXT", writer)
        if (writeUnit) {
            writer.writeString("$name$UNIT_NAME_EXT", unit.customName)
        }
    }

    override fun toString(): String {
        // Avoid outputs like "1.0" by returning simply "1"
        return if (floor(value) == value) {
            "${value.toLong()} ${magnitude.denotation}${unit.symbol}"
        } else {
            "$value ${magnitude.denotation}${unit.symbol}"
        }
    }

    /**
     * The value in [unit] without [magnitude] applied, e.g. 100_000 for "100 KΩ"
     */
    val baseValue: Double by lazy {
        if (magnitude.inverse) {
            value / magnitude.factor
        } else {
            magnitude.factor * value
        }
    }

    val northBaseValue: Double by lazy {
        // This works only for normalized MagnitudeValues!
        // TODO Should MagnitudeValue always be normalized?
        if (magnitude.inverse) {
            if (baseValue < 10.0 / magnitude.factor) {
                1.0 / magnitude.factor
            } else if (baseValue < 100.0 / magnitude.factor) {
                10.0 / magnitude.factor
            } else {
                100.0 / magnitude.factor
            }
        } else {
            if (baseValue < 10 * magnitude.factor) {
                magnitude.factor.toDouble()
            } else if (baseValue < 100 * magnitude.factor) {
                10.0 * magnitude.factor
            } else  {
                100.0 * magnitude.factor
            }
        }
    }

    /**
     * Calculates the value converted to the specified [magnitude], e.g. "0.0095" [Magnitude.Mega] for "9.5 KHz".
     */
    fun baseValueInMagnitude(magnitude: Magnitude): Double =
        if (magnitude.inverse) {
            baseValue * magnitude.factor
        } else {
            baseValue / magnitude.factor
        }

    /**
     * Creates a normalized version of this [MagnitudeValue] whose [Magnitude] is adjusted such that its [value]
     * is between 1 and 999 and truncated to 3 significant digits. Example: 0.5672 milli becomes 567 micro.
     *
     * @param snap snap if `true`, the truncated value is also snapped to one of the nine scale values,
     * e.g. 4, 40, or 400.
     * @throws IllegalArgumentException if the normalized value would be outside allowed limits
     */
    fun normalize(snap: Boolean = false): MagnitudeValue {
        if (baseValue >= 1) {
            return when {
                baseValue < Kilo.factor -> ensureMinimumMagnitude(truncate(baseValue, snap), One, unit)
                baseValue < Mega.factor -> ensureMinimumMagnitude(truncate(baseValue / Kilo.factor, snap), Kilo, unit)
                baseValue < Giga.factor -> ensureMinimumMagnitude(truncate(baseValue / Mega.factor, snap), Mega, unit)
                else -> MagnitudeValue(truncate(baseValue / Giga.factor, snap), Giga, unit)
            }
        } else {
            return when {
                baseValue >= 1.0 / Kilo.factor -> ensureMinimumMagnitude(truncate(baseValue * Kilo.factor, snap), Milli, unit)
                baseValue >= 1.0 / Mega.factor -> ensureMinimumMagnitude(truncate(baseValue * Mega.factor, snap), Micro, unit)
                baseValue >= 1.0 / Giga.factor -> ensureMinimumMagnitude(truncate(baseValue * Giga.factor, snap), Nano, unit)
                baseValue >= 1.0 / Tera.factor -> ensureMinimumMagnitude(truncate(baseValue * Tera.factor, snap), Pico, unit)
                else -> this
            }
        }
    }

    private fun ensureMinimumMagnitude(value: Double, magnitude: Magnitude, unit: SIUnit): MagnitudeValue =
        if (magnitude.ordinal >= unit.minimumMagnitude.ordinal) {
            MagnitudeValue(value, magnitude, unit)
        } else {
            throw IllegalArgumentException()
        }

    /**
     * Truncates [value] to [DIGIT_COUNT] significant digits, no matter before or after the comma.
     * @param snap if `true`, the truncated value is also snapped to one of the nine scale values,
     * e.g. 4, 40, or 400.
     */
    private fun truncate(value: Double, snap: Boolean): Double {
        if (value == 0.0) {
            return 0.0
        }
        val d = ceil(log10(if (value < 0) -value else value))
        val factor = 10.0.pow(DIGIT_COUNT - d.toInt())
        val shifted = floor(value * factor).toLong()

        val truncated = shifted / factor

        return if (snap) {
            if (truncated < 10) {
                if (truncated >= 9) {
                    floor(value)
                } else {
                    round(value)
                }
            } else if (truncated < 100) {
                if (truncated >= 90) {
                    floor(truncated / 10) * 10
                } else {
                    round(truncated / 10) * 10
                }
            } else if (truncated < 1000) {
                if (truncated >= 900) {
                    floor(truncated / 100) * 100
                } else {
                    round(truncated / 100) * 100
                }
            } else {
                truncated
            }
        } else {
            truncated
        }
    }
}

