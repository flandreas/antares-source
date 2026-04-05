package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MemoryContentsTest : AbstractFileBasedTest() {

    private lateinit var input: DigitalCircuitInOut
    private lateinit var output: DigitalCircuitInOut

    @BeforeTest
    fun openAndStartCircuit() {
        openCircuit(UUID("58fdc6b5-39e7-425d-a91f-8f1899d19f30"))

        input = openedCircuitView.graph!!.withId(4) as DigitalCircuitInOut
        output = openedCircuitView.graph!!.withId(6) as DigitalCircuitInOut

        startSimulation()
        processUntilQueueIsEmpty()
    }

    @Test
    fun shouldLoadMemoryStorable() {
        assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 17), output.signal)
    }
}