package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.analog.AnalogPort
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.view.output.LightColor
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
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
            minCurrent = MagnitudeValue(5, Magnitude.Milli, SIUnit.Ampere)
            maxCurrent = MagnitudeValue(20, Magnitude.Milli, SIUnit.Ampere)
            lightColor = LightColor.YELLOW
            (model.getPort<AnalogSignal>() as AnalogPort).current = 0.024
        }
    }
}