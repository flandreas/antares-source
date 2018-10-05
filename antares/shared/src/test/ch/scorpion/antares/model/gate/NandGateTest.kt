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
 * Unit tets for [NandGate].
 */
class NandGateTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    private val signalHandler = ForwardSignalHandler()
    private val vertice = CalculatingVertice("Nand", NandCalculator<CalculatingVertice>())

    init {
        vertice.addPort(DigitalPortImpl.createInput("a"))
        vertice.addPort(DigitalPortImpl.createInput("b"))
        vertice.addPort(DigitalPortImpl.createOutput())
    }

    @Test
    fun shouldBeFalseWithTrueInputs() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(true), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(true), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertThat(output.getOutgoingSignal()!!.bitAt(0), `is`(Bit.False))
    }

    @Test
    fun shouldBeTrueWithFalseInputs() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(false), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(false), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertThat(output.getOutgoingSignal()!!.bitAt(0), `is`(Bit.True))
    }

    @Test
    fun shouldBeTrueWithMixedInputs() {
        val inputA = vertice.getInput<DigitalSignal>("a")
        inputA.setIncomingSignal(Word.of(true), signalHandler)
        val inputB = vertice.getInput<DigitalSignal>("b")
        inputB.setIncomingSignal(Word.of(false), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertThat(output.getOutgoingSignal()!!.bitAt(0), `is`(Bit.True))
    }
}
