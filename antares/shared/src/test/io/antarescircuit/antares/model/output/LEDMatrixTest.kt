package io.antarescircuit.antares.model.output

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.execution.SignalHandler
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LEDMatrixTest {

	private val signalHandler = mock<SignalHandler>(MockMode.autofill)

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldBuffer1x1() {
		val ledMatrix = LEDMatrix(BitWidth.BW_1, BitWidth.BW_1)
		ledMatrix.columnPort.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_1, 1), signalHandler)
		ledMatrix.rowPort.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_1, 1), signalHandler)
		ledMatrix.act(signalHandler, ledMatrix.createActorData(ledMatrix.rowPort))

		assertTrue(ledMatrix.isOn(0, 0))
	}

	@Test
	fun shouldBuffer2x2() {
		val ledMatrix = LEDMatrix(BitWidth.BW_2, BitWidth.BW_2)
		ledMatrix.columnPort.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_2, 1), signalHandler)
		ledMatrix.rowPort.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_2, 2), signalHandler)
		ledMatrix.act(signalHandler, ledMatrix.createActorData(ledMatrix.rowPort))

		assertFalse(ledMatrix.isOn(0, 0))
		assertTrue(ledMatrix.isOn(0, 1))
	}
}