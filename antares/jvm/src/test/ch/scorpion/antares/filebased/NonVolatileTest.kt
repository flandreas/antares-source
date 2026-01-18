package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NonVolatileTest : AbstractFileBasedTest() {

    private lateinit var a: DigitalCircuitInOut
    private lateinit var clk: DigitalCircuitInOut
    private lateinit var wr: DigitalCircuitInOut
    private lateinit var dIn: DigitalCircuitInOut
    private lateinit var dOut: DigitalCircuitInOut

    @BeforeTest
    fun openAndStartCircuit() {
        GraphModelModule.nonVolatileService.delete(UUID("654d580e-5b1e-4b68-b5c2-61fe32e73236"))
        openCircuit(UUID("654d580e-5b1e-4b68-b5c2-61fe32e73236"))

        a = openedCircuitView.graph!!.withId(2) as DigitalCircuitInOut
        clk = openedCircuitView.graph!!.withId(4) as DigitalCircuitInOut
        wr = openedCircuitView.graph!!.withId(5) as DigitalCircuitInOut
        dIn = openedCircuitView.graph!!.withId(8) as DigitalCircuitInOut
        dOut = openedCircuitView.graph!!.withId(12) as DigitalCircuitInOut


        startSimulation()
        scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)
    }

    @AfterTest
    fun cleanup() {
        GraphModelModule.nonVolatileService.delete(UUID("654d580e-5b1e-4b68-b5c2-61fe32e73236"))
    }

    @Test
    fun shouldLoadNonVolatileData() {
        // Store
        a.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_4, 3UL), scheduler)
        dIn.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 42UL), scheduler)
        wr.setIncomingSignal(DigitalSignalFactory.of(true), scheduler)
        processUntilQueueIsEmpty()
        clk.setIncomingSignal(DigitalSignalFactory.of(true), scheduler)
        processUntilQueueIsEmpty()
        stopSimulation()

        // Load
        startSimulation()
        processUntilQueueIsEmpty()
        a.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_4, 3UL), scheduler)
        processUntilQueueIsEmpty()

        assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 42UL), dOut.signal)
    }
}