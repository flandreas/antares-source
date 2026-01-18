package ch.scorpion.antares.view.analog

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.analog.AnalogSignal
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AnalogSignalHistoryYAxisTest {

	companion object {

		private const val INSET = 10
		private const val DEFAULT_VALUE_INSET = 20
		private const val HEIGHT = 100
	}

	private lateinit var yAxis: AnalogSignalHistoryYAxis

	@BeforeTest
	fun setup() {
		AntaresTestRule.configure()
		yAxis = AnalogSignalHistoryYAxis(mock(MockMode.autofill), INSET, INSET, AnalogSignal.HIGH_VOLTAGE, DEFAULT_VALUE_INSET)
		yAxis.setBounds(0, 0, AnalogSignalHistoryYAxis.WIDTH, HEIGHT)
	}

	@Test
	fun baselineYShouldBeAtBottomInsetByDefault() {
		assertEquals(HEIGHT - INSET, yAxis.baselineY.toInt())
	}

	@Test
	fun shouldRenderDefaultValueAtDefaultValueTopInset() {
		assertEquals(HEIGHT - 2 * INSET - DEFAULT_VALUE_INSET, -yAxis.signalY(AnalogSignal.HIGH_VOLTAGE).toInt())
	}
}