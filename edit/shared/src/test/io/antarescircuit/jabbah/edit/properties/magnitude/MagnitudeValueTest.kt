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
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.Farad
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.Hertz
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.Ohm
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.Second
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.Volt
import io.antarescircuit.jabbah.io.AbstractStorable
import io.antarescircuit.jabbah.io.IOModule
import io.antarescircuit.jabbah.io.Reference
import io.antarescircuit.jabbah.io.ReferenceResolver
import io.antarescircuit.jabbah.io.StorableCloner
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MagnitudeValueTest {

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
    fun shouldCalculateBaseValue() {
        assertEquals(1000.0, MagnitudeValue(1, Kilo, Ohm).baseValue)
        assertEquals(1000.0, MagnitudeValue(1, Kilo, SIUnit.Henry).baseValue)
        assertEquals(0.5, MagnitudeValue(500, Milli, SIUnit.Henry).baseValue)
        assertEquals(0.000001, MagnitudeValue(1, Micro, SIUnit.Henry).baseValue)
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
    fun shouldReadAndWrite() {
        val testStorable = TestStorable()
        testStorable.resistance = MagnitudeValue(8, Kilo, Ohm)

        val clone = StorableCloner.clone(testStorable)

        with (clone.resistance) {
            assertEquals(8.0, value)
            assertEquals(Kilo, magnitude)
            assertEquals(Ohm, unit)
            assertEquals(8_000.0, baseValue)
        }
    }

    @Test
    fun shouldParsePeriodOrFrequency() {
        assertEquals(
            MagnitudeValue(3, Kilo, Hertz),
            MagnitudeValueParser.parseWithUnits("3 kHz", Second, Hertz)
        )
        assertEquals(
            MagnitudeValue(500, Milli, Second),
            MagnitudeValueParser.parseWithUnits("500 m", Second, Hertz)
        )

        // No unit. Time evaluated before Frequency. Time has Nano default magnitude.
        assertEquals(
            MagnitudeValue(500, Nano, Second),
            MagnitudeValueParser.parseWithUnits("500", Second, Hertz)
        )
    }

    @Test
    fun shouldYieldDifferentBaseUnit() {
        assertEquals(3_000.0, MagnitudeValue(3, One, Second).baseValueInMagnitude(Milli))
        assertEquals(0.0095, MagnitudeValue(9.5, Kilo, Hertz).baseValueInMagnitude(Mega))
    }

    @Test
    fun shouldCopyWithBaseValue() {
        assertEquals(MagnitudeValue(22, Kilo, Ohm), MagnitudeValue(20, Kilo, Ohm).withBaseValue(22_000.0))
    }

    @Test
    fun shouldNormalizeLargerOne() {
        assertEquals(MagnitudeValue(480, One, Volt), MagnitudeValue(480, One, Volt).normalize())
        assertEquals(MagnitudeValue(1.23, Kilo, Volt), MagnitudeValue(1234.0, One, Volt).normalize())
        assertEquals(MagnitudeValue(1.23, Mega, Volt), MagnitudeValue(1234.0, Kilo, Volt).normalize())
        assertEquals(MagnitudeValue(1.23, Giga, Volt), MagnitudeValue(1234.0, Mega, Volt).normalize())
    }

    @Test
    fun shouldNormalizeSmallerOne() {
        assertEquals(MagnitudeValue(5.67, Milli, Volt), MagnitudeValue(0.005672, One, Volt).normalize())
        assertEquals(MagnitudeValue(56.7, Milli, Volt), MagnitudeValue(0.05672, One, Volt).normalize())
        assertEquals(MagnitudeValue(567, Milli, Volt), MagnitudeValue(0.5672, One, Volt).normalize())
        assertEquals(MagnitudeValue(5.67, One, Volt), MagnitudeValue(5.672, One, Volt).normalize())
        assertEquals(MagnitudeValue(5.67, Milli, Volt), MagnitudeValue(5.672, Milli, Volt).normalize())

        assertEquals(MagnitudeValue(567, Micro, Volt), MagnitudeValue(0.5672, Milli, Volt).normalize())
        assertEquals(MagnitudeValue(56.7, Micro, Volt), MagnitudeValue(0.05672, Milli, Volt).normalize())
        assertEquals(MagnitudeValue(5.67, Micro, Volt), MagnitudeValue(0.005672, Milli, Volt).normalize())

        assertEquals(MagnitudeValue(567, Nano, Farad), MagnitudeValue(0.0005672, Milli, Farad).normalize())
        assertEquals(MagnitudeValue(56.7, Nano, Farad), MagnitudeValue(0.00005672, Milli, Farad).normalize())
        assertEquals(MagnitudeValue(5.67, Nano, Farad), MagnitudeValue(0.000005672, Milli, Farad).normalize())
        assertEquals(MagnitudeValue(567, Pico, Farad), MagnitudeValue(0.0000005672, Milli, Farad).normalize())
    }

    @Test
    fun shouldNormalizeSnapped() {
        assertEquals(MagnitudeValue(6, Milli, Volt), MagnitudeValue(0.005672, One, Volt).normalize(true))
        assertEquals(MagnitudeValue(60, Milli, Volt), MagnitudeValue(0.05672, One, Volt).normalize(true))
        assertEquals(MagnitudeValue(600, Milli, Volt), MagnitudeValue(0.5672, One, Volt).normalize(true))
    }

    @Test
    fun shouldCalculateNorthBaseValue() {
        assertEquals(0.001, MagnitudeValue(5, Milli, Volt).northBaseValue)
        assertEquals(0.01, MagnitudeValue(54, Milli, Volt).northBaseValue)
        assertEquals(0.1, MagnitudeValue(543, Milli, Volt).northBaseValue)

        assertEquals(0.000_001, MagnitudeValue(5, Micro, Volt).northBaseValue)
        assertEquals(0.000_01, MagnitudeValue(54, Micro, Volt).northBaseValue)
        assertEquals(0.000_1, MagnitudeValue(543, Micro, Volt).northBaseValue)

        assertEquals(1.0, MagnitudeValue(5, One, Volt).northBaseValue)
        assertEquals(10.0, MagnitudeValue(54, One, Volt).northBaseValue)
        assertEquals(100.0, MagnitudeValue(543, One, Volt).northBaseValue)

        assertEquals(1_000.0, MagnitudeValue(5, Kilo, Volt).northBaseValue)
        assertEquals(10_000.0, MagnitudeValue(54, Kilo, Volt).northBaseValue)
        assertEquals(100_000.0, MagnitudeValue(543, Kilo, Volt).northBaseValue)

        assertEquals(1_000_000.0, MagnitudeValue(5, Mega, Volt).northBaseValue)
        assertEquals(10_000_000.0, MagnitudeValue(54, Mega, Volt).northBaseValue)
        assertEquals(100_000_000.0, MagnitudeValue(543, Mega, Volt).northBaseValue)

        assertEquals(1_000_000_000.0, MagnitudeValue(5, Giga, Ohm).northBaseValue)
        assertEquals(10_000_000_000.0, MagnitudeValue(54, Giga, Ohm).northBaseValue)
        assertEquals(100_000_000_000.0, MagnitudeValue(543, Giga, Ohm).northBaseValue)

    }

    private fun assertParseValue(text: String, unit: SIUnit, value: Double, magnitude: Magnitude, baseValue: Double, toString: String) {
        val magnitudeValue = MagnitudeValueParser.parse(text, unit)
        assertEquals(value, magnitudeValue.value)
        assertEquals(magnitude, magnitudeValue.magnitude)
        assertEquals(baseValue, magnitudeValue.baseValue)
        assertEquals(toString, magnitudeValue.toString())
    }

    class TestStorable: AbstractStorable() {

        lateinit var resistance: MagnitudeValue

        override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

        override fun write(writer: StoreWriter) {
            resistance.write("resistance", writer)
        }

        override fun read(reader: StoreReader) {
            resistance = MagnitudeValue.read("resistance", reader, Ohm)
        }
    }
}