package io.antarescircuit.jabbah.edit.properties.magnitude

import io.antarescircuit.jabbah.edit.EditTestRule
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude.Giga
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude.Kilo
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude.Mega
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude.Micro
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude.Milli
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude.Nano
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude.One
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude.Pico
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValueTest.TestStorable
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.Farad
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.Ohm
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.Volt
import io.antarescircuit.jabbah.io.IOModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MagnitudeValueParserTest {

    init {
        EditTestRule.configure()
        IOModule.typeMap.register("testStorable", TestStorable::class)
    }

    @Test
    fun shouldParse() {
        MagnitudeValueParser.parse("100K", Ohm).apply {
            assertEquals(100.0, value)
            assertEquals(Kilo, magnitude)
            assertEquals(100_000.0, baseValue)
            assertEquals("100 KΩ", toString())
        }
    }

    @Test
    fun shouldParseWithUnit() {
        MagnitudeValueParser.parse("100 KΩ", Ohm).apply {
            assertEquals(100.0, value)
            assertEquals(Kilo, magnitude)
            assertEquals(100_000.0, baseValue)
            assertEquals("100 KΩ", toString())
        }
    }

    @Test
    fun shouldParseDouble() {
        MagnitudeValueParser.parse("4.6 K", Ohm).apply {
            assertEquals(4.6, value)
            assertEquals(Kilo, magnitude)
            assertEquals(4_600.0, baseValue)
            assertEquals("4.6 KΩ", toString())
        }
    }

    @Test
    fun shouldParseKilo() {
        assertParseValue("100", Ohm, 100.0, One, 100.0, "100 Ω")
        assertParseValue("1 k", Ohm, 1.0, Kilo, 1_000.0, "1 KΩ")
        assertParseValue("200 K", Ohm, 200.0, Kilo, 200_000.0, "200 KΩ")
        assertParseValue("33K", Ohm, 33.0, Kilo, 33_000.0, "33 KΩ")

        assertParseValue("5 k", Volt, 5.0, Kilo, 5_000.0, "5 KV")
    }

    @Test
    fun shouldParseMega() {
        assertParseValue("1 M", Ohm, 1.0, Mega, 1_000_000.0, "1 MΩ")
        assertParseValue("200 M", Ohm, 200.0, Mega, 200_000_000.0, "200 MΩ")
        assertParseValue("33M", Ohm, 33.0, Mega, 33_000_000.0, "33 MΩ")
    }

    @Test
    fun shouldParseGiga() {
        assertParseValue("1 g", Ohm, 1.0, Giga, 1_000_000_000.0, "1 GΩ")
        assertParseValue("200 G", Ohm, 200.0, Giga, 200_000_000_000.0, "200 GΩ")
        assertParseValue("33G", Ohm, 33.0, Giga, 33_000_000_000.0, "33 GΩ")
    }

    @Test
    fun shouldParseMilli() {
        assertParseValue("500", Farad, 500.0, Micro, 500E-6, "500 µF")
        assertParseValue("250 m", Farad, 250.0, Milli, 250E-3, "250 mF")
        assertParseValue("1m", Farad, 1.0, Milli, 1.0E-3, "1 mF")
    }

    @Test
    fun shouldParseMicro() {
        assertParseValue("1", SIUnit.Henry, 1.0, One, 1.0, "1 H")
        assertParseValue("250 u", SIUnit.Henry, 250.0, Micro, 250E-6, "250 µH")
        assertParseValue("1u", SIUnit.Henry, 1.0, Micro, 1.0E-6, "1 µH")
        assertParseValue("2 µ", SIUnit.Henry, 2.0, Micro, 2.0E-6, "2 µH")
    }

    @Test
    fun shouldParseNano() {
        assertParseValue("250 n", SIUnit.Henry, 250.0, Nano, 250.0E-9, "250 nH")
        assertParseValue("1n", SIUnit.Henry, 1.0, Nano, 1.0E-9, "1 nH")
    }

    @Test
    fun shouldParsePico() {
        assertParseValue("250 p", Farad, 250.0, Pico, 250.0E-12, "250 pF")
        assertParseValue("1p", Farad, 1.0, Pico, 1.0E-12, "1 pF")

        assertFailsWith(IllegalArgumentException::class) {
            MagnitudeValueParser.parse("5 p", SIUnit.Henry)
        }
    }

    @Test
    fun shouldParseNegativeValue() {
        assertParseValue("-15", Volt, -15.0, One, -15.0, "-15 V")
        assertParseValue("-150m", Volt, -150.0, Milli, -0.15, "-150 mV")
    }

    private fun assertParseValue(text: String, unit: SIUnit, value: Double, magnitude: Magnitude, baseValue: Double, toString: String) {
        val magnitudeValue = MagnitudeValueParser.parse(text, unit)
        assertEquals(value, magnitudeValue.value)
        assertEquals(magnitude, magnitudeValue.magnitude)
        assertEquals(baseValue, magnitudeValue.baseValue)
        assertEquals(toString, magnitudeValue.toString())
    }

}