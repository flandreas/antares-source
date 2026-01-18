package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class TriStateBufferGateTest {

    private val signalHandler = ForwardSignalHandler(CurrentSystemSpeedCategory(SystemSpeed()))
	private val positiveGate: TriStateBufferGate
	private val negativeGate: TriStateBufferGate

	init {
		AntaresTestRule.configure()
		positiveGate = TriStateBufferGate(BitWidth.BW_1, Logic.POSITIVE)
		negativeGate = TriStateBufferGate(BitWidth.BW_1, Logic.NEGATIVE)
	}

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
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertPositive(False, False, Undefined)
		assertPositive(False, True, Undefined)
		assertPositive(False, Undefined, Undefined)
		assertPositive(False, Error, Undefined)

		assertPositive(True, False, False)
		assertPositive(True, True, True)
		assertPositive(True, Undefined, False)
		assertPositive(True, Error, Error)

		// Treat undefined control input according to CurrentUndefinedGateInputBehavior
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
	fun shouldFulfillNegativeLogicTruthTable() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertNegative(False, False, False)
		assertNegative(False, True, True)
		assertNegative(False, Undefined, False)
		assertNegative(False, Error, Error)

		assertNegative(True, False, Undefined)
		assertNegative(True, True, Undefined)
		assertNegative(True, Undefined, Undefined)
		assertNegative(True, Error, Undefined)

		// Treat undefined control input according to CurrentUndefinedGateInputBehavior
		assertNegative(Undefined, False, False)
		assertNegative(Undefined, True, True)
		assertNegative(Undefined, Undefined, False)
		assertNegative(Undefined, Error, Error)

		assertNegative(Error, False, Error)
		assertNegative(Error, True, Error)
		assertNegative(Error, Undefined, Error)
		assertNegative(Error, Error, Error)
	}
}
