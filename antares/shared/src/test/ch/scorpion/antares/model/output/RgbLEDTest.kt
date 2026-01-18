package ch.scorpion.antares.model.output

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
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