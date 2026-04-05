package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.TestGate
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.execution.ForwardSignalHandler
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
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