package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.Bit.*
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.execution.ForwardSignalHandler
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class TransistorTest {

	private val signalHandler = ForwardSignalHandler(CurrentSystemSpeedCategory(SystemSpeed()))
	private lateinit var transistor: Transistor

	init {
		AntaresTestRule.configure()
	}

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
		(transistor.gatePort as DigitalPort).setIncomingSignal(DigitalSignalFactory.of(control), signalHandler)
		transistor.inputPort.setIncomingSignal(DigitalSignalFactory.of(data), signalHandler)
		assertEquals(result, transistor.outputPort.getOutgoingSignal()!!.bitAt(0))
	}
}