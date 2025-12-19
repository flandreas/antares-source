package ch.scorpion.antares.view.analog

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.analog.AnalogPort
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.output.LightColor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

class AnalogLEDViewTest {

    @BeforeTest
    fun setup() {
        AntaresTestRule.configure()
    }

    @Test
    fun bothShouldHaveHalo() {
        val led1 = createLEDView()
        val led2 = createLEDView()

        val halo1 = led1.haloPaint
        val halo2 = led2.haloPaint

        assertSame(halo1, halo2)
    }

    private fun createLEDView(): AnalogLEDView {
        return AnalogLEDView().apply {
            minCurrent = 0.005
            maxCurrent = 0.02
            lightColor = LightColor.YELLOW
            (model.getPort<AnalogSignal>() as AnalogPort).current = 0.024
        }
    }
}