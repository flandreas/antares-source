package ch.scorpion.antares.view.analog

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.analog.AnalogPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.output.LightColor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LightBulbViewTest {

    private lateinit var view: LightBulbView

    @BeforeTest
    fun setup() {
        AntaresTestRule.configure()
        view = LightBulbView()
    }

    @Test
    fun shouldNotGlowBelowMinimumCurrent() {
        view.minCurrent = 0.05
        view.maxCurrent = 0.1
        (view.model.getPort<DigitalSignal>() as AnalogPort).current = 0.04

        assertEquals(0.0F, view.executionLightFactor)
    }

    @Test
    fun shouldGlowMaxAboveMaximumCurrent() {
        view.minCurrent = 0.05
        view.maxCurrent = 0.1
        (view.model.getPort<DigitalSignal>() as AnalogPort).current = 0.15

        assertEquals(1.0F, view.executionLightFactor)
    }

    @Test
    fun shouldGlowMediumAtMediumCurrent() {
        view.minCurrent = 0.0
        view.maxCurrent = 0.1
        (view.model.getPort<DigitalSignal>() as AnalogPort).current = 0.05

        assertEquals(0.5F, view.executionLightFactor)
    }

    @Test
    fun shouldGlowMediumAtNegativeMediumCurrent() {
        view.minCurrent = 0.0
        view.maxCurrent = 0.1
        (view.model.getPort<DigitalSignal>() as AnalogPort).current = -0.05

        assertEquals(0.5F, view.executionLightFactor)
    }

}