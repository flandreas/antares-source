package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.TestGate
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import kotlin.test.assertEquals

abstract class AbstractGateCalculatorTest(protected val gateType: LogicGateType) {

	private val gate: TestGate
	protected val signalHandler = ForwardSignalHandler(CurrentSystemSpeedCategory(SystemSpeed()))

	init {
		AntaresTestRule.configure()
		gate = TestGate(gateType)
		gate.getInput<DigitalSignal>(1).name = "a"
		gate.getInput<DigitalSignal>(2).name = "b"
	}

	protected fun assertOneInput(a: Bit, result: Bit) {
		gate.getInput<DigitalSignal>().setIncomingSignal(DigitalSignalFactory.of(a), signalHandler)
		assertEquals(result, gate.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0))
	}

	protected fun assertTwoInput(a: Bit, b: Bit, result: Bit) {
		gate.getInput<DigitalSignal>("a").setIncomingSignal(DigitalSignalFactory.of(a), signalHandler)
		gate.getInput<DigitalSignal>("b").setIncomingSignal(DigitalSignalFactory.of(b), signalHandler)
		assertEquals(result, gate.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0))
	}
}