package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.TestCalculatingVertice
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [BufferCalculator].
 */
class BufferCalculatorTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val signalHandler = ForwardSignalHandler()
	private val vertice = TestCalculatingVertice(BufferCalculator())

	init {
		vertice.addPort(DigitalPortImpl.createInput())
		vertice.addPort(DigitalPortImpl.createOutput())
	}

	@Test
	fun shouldFulfillTruthTable() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertOneInput(Bit.False, Bit.False)
		assertOneInput(Bit.True, Bit.True)
		assertOneInput(Bit.Undefined, Bit.False)
		assertOneInput(Bit.Error, Bit.Error)
	}

	private fun assertOneInput(a: Bit, result: Bit) {
		vertice.getInput<DigitalSignal>().setIncomingSignal(DigitalSignalFactory.of(a), signalHandler)
		assertEquals(result, vertice.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0))
	}
}
