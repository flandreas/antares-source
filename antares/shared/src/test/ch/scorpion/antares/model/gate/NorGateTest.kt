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
 * Unit tests for [NorCalculator].
 */
class NorCalculatorTest {

    companion object {
	    init {
		    AntaresTestRule.configure()
	    }
    }

    private val signalHandler = ForwardSignalHandler()
    private val vertice = CalculatingVertice("Nor", NorCalculator<CalculatingVertice>())

    init {
        vertice.addPort(DigitalPortImpl.createInput("a"))
        vertice.addPort(DigitalPortImpl.createInput("b"))
        vertice.addPort(DigitalPortImpl.createOutput())
    }

    @Test
    fun shouldBeTrueWithFalseInputs() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(false), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(false), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.True, output.getOutgoingSignal()!!.bitAt(0))
    }

    @Test
    fun shouldBeFalseWithFirstTrueInput() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(false), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(true), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.False, output.getOutgoingSignal()!!.bitAt(0))
    }

    @Test
    fun shouldBeFalseWithSecondTrueInput() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(true), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(false), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.False, output.getOutgoingSignal()!!.bitAt(0))
    }

    @Test
    fun shouldBeFalseWithAllTrueInput() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(true), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(true), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.False, output.getOutgoingSignal()!!.bitAt(0))
    }

    @Test
    fun shouldBeFalseWithFirstUndefinedAndTrueInput() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(true), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.False, output.getOutgoingSignal()!!.bitAt(0))
    }

    @Test
    fun shouldBeFalseWithSecondUndefinedAndTrueInput() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(true), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.False, output.getOutgoingSignal()!!.bitAt(0))
    }

    @Test
    fun shouldBeUndefinedWithFirstUndefinedAndFalseInput() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(false), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.Undefined, output.getOutgoingSignal()!!.bitAt(0))
    }

    @Test
    fun shouldBeUndefinedWithSecondUndefinedAndFalseInput() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(false), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.Undefined, output.getOutgoingSignal()!!.bitAt(0))
    }

    @Test
    fun shouldBeUndefinedWithAllUndefinedAndTrueInput() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertEquals(Bit.Undefined, output.getOutgoingSignal()!!.bitAt(0))
    }
}
