package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class TransistorTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val signalHandler = ForwardSignalHandler()
	private lateinit var transistor: Transistor

	@Test
	fun shouldCalculateNType() {
		transistor = Transistor(TransistorType.N)

		assert(False, False, Undefined)
		assert(False, True, Undefined)
		assert(False, Undefined, Undefined)
		assert(False, Error, Undefined)

		assert(True, False, False)
		assert(True, True, True)
		assert(True, Undefined, Undefined)
		assert(True, Error, Error)

		assert(Undefined, False, Undefined)
		assert(Undefined, True, Undefined)
		assert(Undefined, Undefined, Undefined)
		assert(Undefined, Error, Undefined)

		assert(Error, False, Error)
		assert(Error, True, Error)
		assert(Error, Undefined, Error)
		assert(Error, Error, Error)
	}

	@Test
	fun shouldCalculatePType() {
		transistor = Transistor(TransistorType.P)

		assert(False, False, False)
		assert(False, True, True)
		assert(False, Undefined, Undefined)
		assert(False, Error, Error)

		assert(True, False, Undefined)
		assert(True, True, Undefined)
		assert(True, Undefined, Undefined)
		assert(True, Error, Undefined)

		assert(Undefined, False, False)
		assert(Undefined, True, True)
		assert(Undefined, Undefined, Undefined)
		assert(Undefined, Error, Error)

		assert(Error, False, Error)
		assert(Error, True, Error)
		assert(Error, Undefined, Error)
		assert(Error, Error, Error)
	}

	private fun assert(control: Bit, data: Bit, result: Bit) {
		transistor.getGatePort().setIncomingSignal(DigitalSignalFactory.of(control), signalHandler)
		transistor.getSourcePort().setIncomingSignal(DigitalSignalFactory.of(data), signalHandler)
		assertEquals(result, transistor.getDrainPort().getOutgoingSignal()!!.bitAt(0))
	}
}