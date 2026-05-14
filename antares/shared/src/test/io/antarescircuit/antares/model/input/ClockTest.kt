package io.antarescircuit.antares.model.input

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.io.StorableCloner
import kotlin.test.Test
import kotlin.test.assertEquals

class ClockTest {

    init {
        AntaresTestRule.configure()
    }

    @Test
    fun shouldStore1Hz() {
        val clock = Clock()
        clock.periodOrFrequency = MagnitudeValue(1, Magnitude.One, SIUnit.Hertz)
        val clone = StorableCloner.clone(clock)
        assertEquals(LongValueImpl(Magnitude.Nano.factor), clone.propagationDelay)
    }

    @Test
    fun shouldReadBackwardCompatible() {
        assertReadBackwardCompatible(1.0, Magnitude.One, SIUnit.Second, 1_000_000_000L, "<clock _id='3' id='1' delay='1000000000' unit='s'/>")
        assertReadBackwardCompatible(500.0, Magnitude.Milli, SIUnit.Second, 500_000_000L, "<clock _id='3' id='1' delay='500000000' unit='ms'/>")
        assertReadBackwardCompatible(500.0, Magnitude.Micro, SIUnit.Second, 500_000L, "<clock _id='3' id='1' delay='500000' unit='us'/>")
        assertReadBackwardCompatible(500.0, Magnitude.Nano, SIUnit.Second, 500L, "<clock _id='3' id='1' delay='500' unit='ns'/>")
        // Pico is below minimum
        assertReadBackwardCompatible(10.0, Magnitude.One, SIUnit.Hertz, 100_000_000, "<clock _id='3' id='1' delay='100000000' unit='Hz'/>")
        assertReadBackwardCompatible(10.0, Magnitude.Kilo, SIUnit.Hertz, 100_000, "<clock _id='3' id='1' delay='100000' unit='kHz'/>")
        assertReadBackwardCompatible(10.0, Magnitude.Mega, SIUnit.Hertz, 100, "<clock _id='3' id='1' delay='100' unit='MHz'/>")
        assertReadBackwardCompatible(1.0, Magnitude.Giga, SIUnit.Hertz, 1, "<clock _id='3' id='1' delay='1' unit='GHz'/>")
    }

    /** From example "Sine Wave": This is from before even "unit" was introduced. */
    @Test
    fun shouldReadBackwardCompatibleBeforeUnit() {
        assertReadBackwardCompatible(17.7, Magnitude.Milli, SIUnit.Second, 17_700_000, "<clock _id='44' id='24' delay='17733000'/>")
    }

    private fun assertReadBackwardCompatible(value: Double, magnitude: Magnitude, unit: SIUnit, propDelay: Long, text: String) {
        val clock = StorableCloner.deserialize(text) as Clock
        assertEquals(value, clock.periodOrFrequency.value, "Wrong value")
        assertEquals(magnitude, clock.periodOrFrequency.magnitude, "Wrong magnitude")
        assertEquals(unit, clock.periodOrFrequency.unit, "Wrong unit")
        assertEquals(propDelay, clock.propagationDelay.value, "Wrong propagationDelay")
    }
}