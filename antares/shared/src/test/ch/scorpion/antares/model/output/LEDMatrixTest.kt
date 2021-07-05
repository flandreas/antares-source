package ch.scorpion.antares.model.output

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [LEDMatrix].
 */
class LEDMatrixTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val signalHandler = mockk<SignalHandler>(relaxed = true)

	@Test
	fun shouldBuffer1x1() {
		val ledMatrix = LEDMatrix(BitWidth.BW_1, BitWidth.BW_1)
		ledMatrix.columnPort.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_1, 1), signalHandler)
		ledMatrix.rowPort.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_1, 1), signalHandler)
		ledMatrix.act(signalHandler, GraphActorDataImpl(ledMatrix.rowPort, null))

		assertTrue(ledMatrix.isOn(0, 0))
	}

	@Test
	fun shouldBuffer2x2() {
		val ledMatrix = LEDMatrix(BitWidth.BW_2, BitWidth.BW_2)
		ledMatrix.columnPort.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_2, 1), signalHandler)
		ledMatrix.rowPort.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_2, 2), signalHandler)
		ledMatrix.act(signalHandler, GraphActorDataImpl(ledMatrix.rowPort, null))

		assertFalse(ledMatrix.isOn(0, 0))
		assertTrue(ledMatrix.isOn(0, 1))
	}
}