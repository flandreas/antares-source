package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [OrCalculator].
 */
class OrCalculatorTest {

    companion object {
	    init {
		    AntaresTestRule.configure()
	    }
    }

    private val signalHandler = ForwardSignalHandler()
    private val vertice = CalculatingVertice("library.element.OrGate", OrCalculator<CalculatingVertice>())

    init {
        vertice.addPort(DigitalPortImpl.createInput("a"))
        vertice.addPort(DigitalPortImpl.createInput("b"))
        vertice.addPort(DigitalPortImpl.createOutput())
    }

    @Test
    fun shouldBeTrueWithTrueInputs() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(true), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(true), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.True, output.getOutgoingSignal()!!.bitAt(0))
    }

    @Test
    fun shouldBeFalseWithFalseInputs() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(false), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(false), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.False, output.getOutgoingSignal()!!.bitAt(0))
    }

    @Test
    fun shouldBeTrueWithMixedInputs() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(true), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(false), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.True, output.getOutgoingSignal()!!.bitAt(0))
    }

    @Test
    fun shouldBeErrorWithAllUndefined() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.Error, output.getOutgoingSignal()!!.bitAt(0))
    }

    @Test
    fun shouldBeTrueWithOneUndefined() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(true), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.True, output.getOutgoingSignal()!!.bitAt(0))
    }
}