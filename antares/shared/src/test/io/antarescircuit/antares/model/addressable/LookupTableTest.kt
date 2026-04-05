package io.antarescircuit.antares.model.addressable

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.gate.CurrentUndefinedGateInputBehavior
import io.antarescircuit.antares.model.gate.UndefinedGateInputBehavior
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.execution.SignalHandler
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class LookupTableTest {

    private val lut: LookupTable
    private val signalHandler: SignalHandler = mock(MockMode.autofill)

    init {
        AntaresTestRule.configure()
        lut = LookupTable(dataBitWidth = BitWidth.BW_8)
    }

    @Test
    fun shouldRead2() {
        lut.memory.write(0, 99UL)
        lut.getInput<DigitalSignal>().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_4, 0UL), signalHandler)

        LookupTable.CALCULATOR.calculate(lut, lut.createActorData(lut.getInput<DigitalSignal>()), signalHandler)

        val dataOutput = lut.getOutput<DigitalSignal>()
        assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 99L), dataOutput.getOutgoingSignal())
    }

    @Test
    fun shouldUseUndefinedGateInputBehaviorWithUndefinedAddress() {
        CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0
        lut.memory.write(0, 99UL)
        lut.getInput<DigitalSignal>().setIncomingSignal(DigitalSignalFactory.undefined(BitWidth.BW_4), signalHandler)

        LookupTable.CALCULATOR.calculate(lut, lut.createActorData(lut.getInput<DigitalSignal>()), signalHandler)

        val dataOutput = lut.getOutput<DigitalSignal>()
        assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 99L), dataOutput.getOutgoingSignal())
    }
}