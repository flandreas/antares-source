package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Modulo5CounterJKNegTest : AbstractFileBasedTest() {

    private lateinit var switch: SwitchView
    private lateinit var output: DigitalCircuitInOutView

    @BeforeTest
    fun openAndStartCircuit() {
        openCircuit(UUID("f32ec46c-c2b1-4c19-8049-b45b9edd09e4"))

        switch = openedCircuitView.getWithId(56) as SwitchView
        output = openedCircuitView.getWithId(52) as DigitalCircuitInOutView

        startSimulation()
        proceedUntilQueueIsEmpty()
    }

    @Test
    fun shouldResetAt5() {
        assertClockUpDownTo(1UL)
        assertClockUpDownTo(2UL)
        assertClockUpDownTo(3UL)
        assertClockUpDownTo(4UL)
        assertClockUpDownTo(0UL)
    }

    private fun assertClockUpDownTo(value: ULong) {
        clockUpDown()
        assertOutput(value)
    }

    private fun assertOutput(value: ULong) {
        assertEquals(value, output.model.signal!!.getValue())
    }

    private fun clockUpDown() {
        switch.model.toggle(scheduler)
        proceedUntilQueueIsEmpty()
        switch.model.toggle(scheduler)
        proceedUntilQueueIsEmpty()
    }
}