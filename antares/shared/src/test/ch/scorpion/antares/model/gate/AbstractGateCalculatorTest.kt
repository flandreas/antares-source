package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.TestCalculatingVertice
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import kotlin.test.assertEquals

abstract class AbstractGateCalculatorTest(
	calculator: VerticeCalculator<CalculatingVertice>
) {

	private val gate = TestCalculatingVertice(calculator)
	private val signalHandler = ForwardSignalHandler()

	init {
		gate.addPort(DigitalPortImpl.createInput("a"))
		gate.addPort(DigitalPortImpl.createInput("b"))
		gate.addPort(DigitalPortImpl.createOutput())
	}

	protected fun assertTwoInput(a: Bit, b: Bit, result: Bit) {
		gate.getInput<DigitalSignal>("a").setIncomingSignal(Word.of(a), signalHandler)
		gate.getInput<DigitalSignal>("b").setIncomingSignal(Word.of(b), signalHandler)
		assertEquals(result, gate.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0))
	}
}