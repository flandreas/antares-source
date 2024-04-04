package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalTunnelTest : AbstractFileBasedTest() {

    companion object {
        init {
            configure()
        }
    }

    private lateinit var input: DigitalCircuitInOut
    private lateinit var output: DigitalCircuitInOut

    @BeforeTest
    fun openAndStartCircuit() {
        openCircuit(UUID("d9183029-8382-4030-982d-b6b22dcce948"))

        input = openedCircuitView.graph!!.withId(2) as DigitalCircuitInOut
        output = openedCircuitView.graph!!.withId(6) as DigitalCircuitInOut

        startSimulation()
    }

    @Test
    fun shouldPropagateGlobalSignals() {
        proceedUntilQueueIsEmpty()
        assertEquals(DigitalSignalFactory.of(true), output.signal)

        input.setIncomingSignal(DigitalSignalFactory.of(true), scheduler)
        proceedUntilQueueIsEmpty()

        assertEquals(DigitalSignalFactory.of(false), output.signal)
    }
}