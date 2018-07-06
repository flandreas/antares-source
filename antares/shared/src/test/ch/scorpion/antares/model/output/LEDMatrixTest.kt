package ch.scorpion.antares.model.output

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import com.nhaarman.mockitokotlin2.mock
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [LEDMatrix].
 */
class LEDMatrixTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    private val signalHandler = mock<SignalHandler>()

    @Test
    fun shouldBuffer1x1() {
        val ledMatrix = LEDMatrix(BitWidth.BW_1, BitWidth.BW_1)
        ledMatrix.columnPort.setIncomingSignal(Word.of(BitWidth.BW_1, 1), signalHandler)
        ledMatrix.rowPort.setIncomingSignal(Word.of(BitWidth.BW_1, 1), signalHandler)
        ledMatrix.act(signalHandler, GraphActorDataImpl(ledMatrix.rowPort, null))

        assertThat(ledMatrix.isOn(0, 0), `is`(true))
    }

    @Test
    fun shouldBuffer2x2() {
        val ledMatrix = LEDMatrix(BitWidth.BW_2, BitWidth.BW_2)
        ledMatrix.columnPort.setIncomingSignal(Word.of(BitWidth.BW_2, 1), signalHandler)
        ledMatrix.rowPort.setIncomingSignal(Word.of(BitWidth.BW_2, 2), signalHandler)
        ledMatrix.act(signalHandler, GraphActorDataImpl(ledMatrix.rowPort, null))

        assertThat(ledMatrix.isOn(0, 0), `is`(false))
        assertThat(ledMatrix.isOn(0, 1), `is`(true))
    }
}