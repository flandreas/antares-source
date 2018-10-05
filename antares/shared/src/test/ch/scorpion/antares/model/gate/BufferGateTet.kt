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
 * Unit tests for [BufferCalculator].
 */
class BufferCalculatorTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    private val signalHandler = ForwardSignalHandler()
    private val vertice = CalculatingVertice("Buffer", BufferCalculator<CalculatingVertice>())

    init {
        vertice.addPort(DigitalPortImpl.createInput())
        vertice.addPort(DigitalPortImpl.createOutput())
    }

    @Test
    fun shouldBeTrueWithTrueInput() {
        val input = vertice.getInput<DigitalSignal>()
        input.setIncomingSignal(Word.of(true), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertThat(output.getOutgoingSignal()!!.bitAt(0), `is`(Bit.True))
    }

    @Test
    fun shouldBeFalseWithFalseInput() {
        val input = vertice.getInput<DigitalSignal>()
        input.setIncomingSignal(Word.of(false), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertThat(output.getOutgoingSignal()!!.bitAt(0), `is`(Bit.False))
    }

    @Test
    fun shouldBeUndefinedWithUndefinedInput() {
        val input = vertice.getInput<DigitalSignal>()
        input.setIncomingSignal(Word.of(Bit.Undefined), signalHandler)

        val output = vertice.getOutput<DigitalSignal>()
        assertThat(output.getOutgoingSignal()!!.bitAt(0), `is`(Bit.Undefined))
    }
}
