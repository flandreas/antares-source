package io.antarescircuit.antares.model.output

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.execution.ForwardSignalHandler
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class RgbLEDTest {

	private val signalHandler = ForwardSignalHandler(CurrentSystemSpeedCategory(SystemSpeed()))

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldCovertValueToColor() {
		val led = RgbLED()
		led.getInput<DigitalSignal>().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_24, (16 * 256 * 256 + 4 * 256).toLong()), signalHandler)

		assertEquals(Color(16, 4, 0), led.color)
	}
}