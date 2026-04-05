package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.dsl.BaseTokenType
import io.antarescircuit.jabbah.base.dsl.Variable
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.base.parser.Token
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresStoringActivationRecordTest {

	@Test
	fun shouldNotStorePartiallyUndefinedSignals() {
		val store = AntaresStoringActivationRecord("Store", null)
		val variable = Variable(TextLocation(0, 0, 0), Token(BaseTokenType.ID, "A"))
		val storedSignal = DigitalSignalFactory.of(BitWidth.BW_2, 3UL)
		store.preset("A", storedSignal)

		store.setValue(variable, DigitalSignalFactory.ofBits(listOf(Bit.True, Bit.Undefined)))

		assertEquals(storedSignal, store.getValue(variable))
	}
}