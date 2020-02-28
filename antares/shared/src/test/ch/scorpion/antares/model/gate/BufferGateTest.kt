package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.TestCalculatingVertice
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
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
	fun shouldBeTrueWithTrueInput() {
		val input = vertice.getInput<DigitalSignal>()
		input.setIncomingSignal(Word.of(true), signalHandler)

		val output = vertice.getOutput<DigitalSignal>()
		assertEquals(Bit.True, output.getOutgoingSignal()!!.bitAt(0))
	}

	@Test
	fun shouldBeFalseWithFalseInput() {
		val input = vertice.getInput<DigitalSignal>()
		input.setIncomingSignal(Word.of(false), signalHandler)

		val output = vertice.getOutput<DigitalSignal>()
		assertEquals(Bit.False, output.getOutgoingSignal()!!.bitAt(0))
	}

	@Test
	fun shouldBeUndefinedWithUndefinedInput() {
		val input = vertice.getInput<DigitalSignal>()
		input.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)

		val output = vertice.getOutput<DigitalSignal>()
		assertEquals(Bit.Undefined, output.getOutgoingSignal()!!.bitAt(0))
	}
}
