package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.model.input.Switch
import io.antarescircuit.antares.model.output.LED
import io.antarescircuit.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration test for GitHub #1089.
 * A wire from an input with start value 1 to an output in a subcircuit remained in state 1 after startup,
 * although the input is forced to 0 by an external switch.
 */
class UnidirectionalSubOutputTest : AbstractFileBasedTest() {

    private lateinit var switch: Switch
    private lateinit var led: LED

    @BeforeTest
    fun openAndStartCircuit() {
        openCircuit(UUID("6fac3421-27fe-43d8-aad9-a8b635431173"))

        switch = openedCircuitView.graph!!.withId(2) as Switch
        led = openedCircuitView.graph!!.withId(4) as LED

        startSimulation()
        processUntilQueueIsEmpty()
    }

    @Test
    fun shouldTurnOffLEDAfterStart() {
        assertFalse(led.isOn)
    }

    @Test
    fun shouldTurnOnLED() {
        switch.toggle(scheduler) // On
        processUntilQueueIsEmpty()

        switch.toggle(scheduler) // Off
        processUntilQueueIsEmpty()

        switch.toggle(scheduler) // On
        processUntilQueueIsEmpty()

        assertTrue(led.isOn)
    }
}