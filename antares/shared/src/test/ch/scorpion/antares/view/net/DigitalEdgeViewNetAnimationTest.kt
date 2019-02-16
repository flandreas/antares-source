package ch.scorpion.antares.view.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [DigitalEdgeViewNetAnimation].
 */
class DigitalEdgeViewNetAnimationTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldNormalizeSpeed() {
		assertEquals(1.0, DigitalEdgeViewNetAnimation.normalizedSpeed(SystemSpeedCategory.Explore.speedRange.endInclusive))
		assertEquals(0.0, DigitalEdgeViewNetAnimation.normalizedSpeed(SystemSpeedCategory.Explore.speedRange.first))
		assertEquals(1.0, DigitalEdgeViewNetAnimation.normalizedSpeed(SystemSpeedCategory.Observe.speedRange.endInclusive))
	}
}