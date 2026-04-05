package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.BitWidth
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DigitalNetTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldNotYieldErrorWithSinglePorts() {
		val net = DigitalNet()
		net.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p1", BitWidth.BW_4))
		assertFalse(net.isError)
	}

	@Test
	fun shouldNotYieldErrorWithEqualBitWidthPorts() {
		val net = DigitalNet()
		net.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p1", BitWidth.BW_4))
		net.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p2", BitWidth.BW_4))
		assertFalse(net.isError)
	}

	@Test
	fun shouldYieldErrorWithDifferentBitWidthPorts() {
		val net = DigitalNet()
		net.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p1", BitWidth.BW_4))
		net.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p2", BitWidth.BW_8))
		assertTrue(net.isError)
	}

	@Test
	fun shouldRecognizeAdaptivePort() {
		val net = DigitalNet()
		val adaptivePort = DigitalPortImpl.createInput(Logic.POSITIVE, "p1", BitWidth.BW_1)
		adaptivePort.isAdaptive = true
		net.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p2", BitWidth.BW_2))
		net.connect(adaptivePort)
		assertFalse(net.isError)
	}
}