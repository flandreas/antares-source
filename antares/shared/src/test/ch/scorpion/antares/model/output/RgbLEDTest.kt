package ch.scorpion.antares.model.output

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

class RgbLEDTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = AntaresTestRule()
	}

	private val signalHandler = ForwardSignalHandler()

	@Test
	fun shouldCovertValueToColor() {
		val led = RgbLED()
		led.getInput<DigitalSignal>().setIncomingSignal(Word.of(BitWidth.BW_24, (16 + 4 * 256).toLong()), signalHandler)

		assertThat(led.color, `is`(Color(16, 4, 0)))
	}
}