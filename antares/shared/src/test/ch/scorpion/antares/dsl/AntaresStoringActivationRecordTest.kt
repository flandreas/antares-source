package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.base.parser.Token
import ch.scorpion.jabbah.base.dsl.DslTokenType
import ch.scorpion.jabbah.base.dsl.Variable
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresStoringActivationRecordTest {

	@Test
	fun shouldNotStorePartiallyUndefinedSignals() {
		val store = AntaresStoringActivationRecord("Store", null)
		val variable = Variable(TextLocation(0, 0, 0), Token(DslTokenType.ID, "A"))
		val storedSignal = DigitalSignalFactory.of(BitWidth.BW_2, 3UL)
		store.preset("A", storedSignal)

		store.setValue(variable, DigitalSignalFactory.ofBits(listOf(Bit.True, Bit.Undefined)))

		assertEquals(storedSignal, store.getValue(variable))
	}
}