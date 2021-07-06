package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.TestCalculatingVertice
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [NotCalculator].
 */
class NotCalculatorTest {

    companion object {
	    init {
		    AntaresTestRule.configure()
	    }
    }

    private val signalHandler = ForwardSignalHandler()
    private val vertice = TestCalculatingVertice(NotCalculator())

    init {
        vertice.addPort(DigitalPortImpl.createInput())
        vertice.addPort(DigitalPortImpl.createOutput())
    }

	@Test
	fun shouldFulfillTruthTable() {
		CurrentOpenGateInputBehaviour.value = OpenGateInputBehavior.Accept

		assertOneInput(False, True)
		assertOneInput(True, False)
		assertOneInput(Undefined, False)
		assertOneInput(Error, Error)
	}

	@Test
	fun shouldCalculateWithErrorForUndefinedInput() {
		CurrentOpenGateInputBehaviour.value = OpenGateInputBehavior.Error

		assertOneInput(False, True)
		assertOneInput(True, False)
		assertOneInput(Undefined, Error)
		assertOneInput(Error, Error)
	}

	private fun assertOneInput(a: Bit, result: Bit) {
		vertice.getInput<DigitalSignal>().setIncomingSignal(DigitalSignalFactory.of(a), signalHandler)
		assertEquals(result, vertice.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0))
	}
}
