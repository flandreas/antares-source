package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.execution.ForwardSignalHandler
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class ConcentratorTest {

    private val signalHandler = ForwardSignalHandler(CurrentSystemSpeedCategory(SystemSpeed()))

    init {
        AntaresTestRule.configure()
    }

    @Test
    fun shouldConcentrateBits() {
        val concentrator = Concentrator(BitWidth.BW_4, BranchCount.BC_4)

        concentrator.getInput<Any>(2).setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
        concentrator.getInput<Any>(3).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
        concentrator.getInput<Any>(4).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
        concentrator.getInput<Any>(5).setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
        concentrator.act(signalHandler, concentrator.createActorData(concentrator.getInput<DigitalSignal>(5)))

        assertEquals(BitWidth.BW_4, (concentrator.getOutput<Any>().getOutgoingSignal() as DigitalSignal).bitWidth)
        assertEquals(6UL, (concentrator.getOutput<Any>().getOutgoingSignal() as DigitalSignal).getValue())
    }

    @Test
    fun shouldConcentrateSubwords() {
        val concentrator = Concentrator(BitWidth.BW_8, BranchCount.BC_4)

        concentrator.getInput<Any>(2).setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_2, 2L), signalHandler)
        concentrator.getInput<Any>(3).setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_2, 3L), signalHandler)
        concentrator.getInput<Any>(4).setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_2, 3L), signalHandler)
        concentrator.getInput<Any>(5).setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_2, 3L), signalHandler)
        concentrator.act(signalHandler, concentrator.createActorData(concentrator.getInput<DigitalSignal>(5)))

        assertEquals(BitWidth.BW_8, (concentrator.getOutput<Any>().getOutgoingSignal() as DigitalSignal).bitWidth)
        assertEquals(254UL, (concentrator.getOutput<Any>().getOutgoingSignal() as DigitalSignal).getValue())
    }
}