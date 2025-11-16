package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration test for GitHub #1089.
 * A wire from an input to an input/output in a subcircuit remained in state "undefined" after startup,
 * although the input is forced to 0 by an external switch.
 */
class BidirectionalSubOutputTest : AbstractFileBasedTest() {

    companion object {
        init {
            configure()
        }
    }

    private lateinit var switch: Switch
    private lateinit var net: DigitalNet

    @BeforeTest
    fun openAndStartCircuit() {
        openCircuit(UUID("cd644a81-7452-4de1-ac8d-a6b40753fa12"))

        switch = openedCircuitView.graph!!.withId(2) as Switch
        //led = openedCircuitView.graph!!.withId(4) as LED
        net = openedCircuitView.graph!!.withId(5) as DigitalNet

        startSimulation()
        processUntilQueueIsEmpty()
    }

    @Test
    fun shouldTurnOffOutgoingNetAfterStart() {
        assertEquals(DigitalSignalFactory.of(false), net.signal)
    }
}