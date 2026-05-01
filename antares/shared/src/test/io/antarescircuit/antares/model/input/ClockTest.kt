package io.antarescircuit.antares.model.input

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.base.LongValueImpl
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
        clock.periodOrFrequency = PeriodOrFrequencyParser.parse("1Hz")
        val clone = StorableCloner.clone(clock)
        assertEquals(LongValueImpl(PeriodOrFrequencyUnit.Hertz.factor), clone.propagationDelay)
    }
}