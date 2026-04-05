package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class MatchedValueTest {

    @Test
    fun shouldPassWithMatchingUndefinedValue() {
        val matchedValue = MatchedValue(
            Value(
                DigitalSignalFactory.allOf(BitWidth.BW_16, Bit.False).withBit(0, Bit.Undefined),
                Value.Type.UNDEFINED
            ),
            DigitalSignalFactory.allOf(BitWidth.BW_16, Bit.False).withBit(0, Bit.Undefined))

        assertEquals(Value.State.PASSED, matchedValue.state)
    }
}