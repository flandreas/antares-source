package ch.scorpion.antares.view.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import org.junit.ClassRule
import org.junit.Test
import org.junit.Assert.*
import org.hamcrest.CoreMatchers.`is`

/**
 * Unit tests for [DigitalEdgeViewNetAnimation].
 */
class DigitalEdgeViewNetAnimationTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    @Test
    fun shouldNormalizeSpeed() {
        assertThat(DigitalEdgeViewNetAnimation.normalizedSpeed(SystemSpeedCategory.Explore.speedRange.endInclusive), `is`(1.0))
        assertThat(DigitalEdgeViewNetAnimation.normalizedSpeed(SystemSpeedCategory.Explore.speedRange.first), `is`(0.0))
        assertThat(DigitalEdgeViewNetAnimation.normalizedSpeed(SystemSpeedCategory.Observe.speedRange.endInclusive), `is`(1.0))
    }
}