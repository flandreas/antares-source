package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.ClassRule
import org.junit.Test


/**
 * Unit tests for [AndCalculator].
 */
class AndCalculatorTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    private val signalHandler = ForwardSignalHandler()
    private val vertice = CalculatingVertice("And", AndCalculator<CalculatingVertice>())

    init {
        vertice.addPort(DigitalPortImpl.createInput("a"))
        vertice.addPort(DigitalPortImpl.createInput("b"))
        vertice.addPort(DigitalPortImpl.createOutput())
    }

    @Test
    fun trueAndTrueShouldBeTrue() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(true), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(true), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertThat(output.getOutgoingSignal()!!.bitAt(0), `is`(Bit.True))
    }

    @Test
    fun falseAndFalseShouldBeFalse() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(false), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(false), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertThat(output.getOutgoingSignal()!!.bitAt(0), `is`(Bit.False))
    }

    @Test
    fun falseAndTrueShouldBeFalse() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(true), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(false), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertThat(output.getOutgoingSignal()!!.bitAt(0), `is`(Bit.False))
    }

    @Test
    fun falseAndUndefinedShouldBeFalse() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(false), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertThat(output.getOutgoingSignal()!!.bitAt(0), `is`(Bit.False))
    }

    @Test
    fun trueAndUndefinedShouldBeError() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(true), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertThat(output.getOutgoingSignal()!!.bitAt(0), `is`(Bit.Error))
    }

    @Test
    fun errorAndAnythingShouldBeError() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(true), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(Bit.Error), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertThat(output.getOutgoingSignal()!!.bitAt(0), `is`(Bit.Error))
    }
}
