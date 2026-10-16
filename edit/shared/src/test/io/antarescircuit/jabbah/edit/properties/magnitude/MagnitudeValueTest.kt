package io.antarescircuit.jabbah.edit.properties.magnitude

import io.antarescircuit.jabbah.edit.EditTestRule
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude.*
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.*
import io.antarescircuit.jabbah.io.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MagnitudeValueTest {

    init {
        EditTestRule.configure()
        IOModule.typeMap.register("testStorable", TestStorable::class)
    }

    @Test
    fun shouldCalculateBaseValue() {
        assertEquals(1000.0, MagnitudeValue(1, Kilo, Ohm).baseValue)
        assertEquals(1000.0, MagnitudeValue(1, Kilo, Henry).baseValue)
        assertEquals(0.5, MagnitudeValue(500, Milli, Henry).baseValue)
        assertEquals(0.000001, MagnitudeValue(1, Micro, Henry).baseValue)
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