package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.TestGate
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import kotlin.test.assertEquals

abstract class AbstractGateCalculatorTest(
	calculator: AbstractDigitalGateCalculator
) {

	private val gate = TestGate(calculator)
	protected val signalHandler = ForwardSignalHandler()

	init {
		gate.getInput<DigitalSignal>(1).name = "a"
		gate.getInput<DigitalSignal>(2).name = "b"
	}

	protected fun assertTwoInput(a: Bit, b: Bit, result: Bit) {
		gate.getInput<DigitalSignal>("a").setIncomingSignal(DigitalSignalFactory.of(a), signalHandler)
		gate.getInput<DigitalSignal>("b").setIncomingSignal(DigitalSignalFactory.of(b), signalHandler)
		assertEquals(result, gate.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0))
	}
}