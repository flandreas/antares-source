package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [TriStateBufferGate].
 */
class TriStateBufferGateTest {

    companion object {
	    init {
		    AntaresTestRule.configure()
	    }
    }

    private val signalHandler = ForwardSignalHandler()
	private val positiveGate = TriStateBufferGate(BitWidth.BW_1, Logic.POSITIVE)
	private val negativeGate = TriStateBufferGate(BitWidth.BW_1, Logic.NEGATIVE)

	private fun assertPositive(control: Bit, data: Bit, result: Bit) {
		positiveGate.getInput<DigitalSignal>("EN").setIncomingSignal(DigitalSignalFactory.of(control), signalHandler)
		positiveGate.getInput<DigitalSignal>(1).setIncomingSignal(DigitalSignalFactory.of(data), signalHandler)
		assertEquals(result, positiveGate.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0))
	}

	private fun assertNegative(control: Bit, data: Bit, result: Bit) {
		negativeGate.getInput<DigitalSignal>("EN").setIncomingSignal(DigitalSignalFactory.of(control), signalHandler)
		negativeGate.getInput<DigitalSignal>(1).setIncomingSignal(DigitalSignalFactory.of(data), signalHandler)
		assertEquals(result, negativeGate.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0))
	}

	@Test
	fun shouldFulfillPositiveLogicTruthTable() {
		CurrentOpenGateInputBehaviour.value = OpenGateInputBehavior.Accept

		assertPositive(False, False, Undefined)
		assertPositive(False, True, Undefined)
		assertPositive(False, Undefined, Undefined)
		assertPositive(False, Error, Undefined)

		assertPositive(True, False, False)
		assertPositive(True, True, True)
		assertPositive(True, Undefined, False)
		assertPositive(True, Error, Error)

		// Treat as control input not asserted
		assertPositive(Undefined, False, Undefined)
		assertPositive(Undefined, True, Undefined)
		assertPositive(Undefined, Undefined, Undefined)
		assertPositive(Undefined, Error, Undefined)

		assertPositive(Error, False, Error)
		assertPositive(Error, True, Error)
		assertPositive(Error, Undefined, Error)
		assertPositive(Error, Error, Error)
	}

	@Test
	fun shouldCalculatePositiveWithErrorForUndefinedInput() {
		CurrentOpenGateInputBehaviour.value = OpenGateInputBehavior.Error

		assertPositive(False, False, Undefined)
		assertPositive(False, True, Undefined)
		assertPositive(False, Undefined, Undefined)
		assertPositive(False, Error, Undefined)

		assertPositive(True, False, False)
		assertPositive(True, True, True)
		assertPositive(True, Undefined, Error)
		assertPositive(True, Error, Error)

		assertPositive(Undefined, False, Error)
		assertPositive(Undefined, True, Error)
		assertPositive(Undefined, Undefined, Error)
		assertPositive(Undefined, Error, Error)

		assertPositive(Error, False, Error)
		assertPositive(Error, True, Error)
		assertPositive(Error, Undefined, Error)
		assertPositive(Error, Error, Error)
	}

	@Test
	fun shouldFulfillNegativeLogicTruthTable() {
		CurrentOpenGateInputBehaviour.value = OpenGateInputBehavior.Accept

		assertNegative(False, False, False)
		assertNegative(False, True, True)
		assertNegative(False, Undefined, False)
		assertNegative(False, Error, Error)

		assertNegative(True, False, Undefined)
		assertNegative(True, True, Undefined)
		assertNegative(True, Undefined, Undefined)
		assertNegative(True, Error, Undefined)

		// Treat as control input not asserted
		assertNegative(Undefined, False, Undefined)
		assertNegative(Undefined, True, Undefined)
		assertNegative(Undefined, Undefined, Undefined)
		assertNegative(Undefined, Error, Undefined)

		assertNegative(Error, False, Error)
		assertNegative(Error, True, Error)
		assertNegative(Error, Undefined, Error)
		assertNegative(Error, Error, Error)
	}

	@Test
	fun shouldCalculateNegativeWithErrorForUndefinedInput() {
		CurrentOpenGateInputBehaviour.value = OpenGateInputBehavior.Error

		assertNegative(False, False, False)
		assertNegative(False, True, True)
		assertNegative(False, Undefined, Error)
		assertNegative(False, Error, Error)

		assertNegative(True, False, Undefined)
		assertNegative(True, True, Undefined)
		assertNegative(True, Undefined, Undefined)
		assertNegative(True, Error, Undefined)

		assertNegative(Undefined, False, Error)
		assertNegative(Undefined, True, Error)
		assertNegative(Undefined, Undefined, Error)
		assertNegative(Undefined, Error, Error)

		assertNegative(Error, False, Error)
		assertNegative(Error, True, Error)
		assertNegative(Error, Undefined, Error)
		assertNegative(Error, Error, Error)
	}
}
