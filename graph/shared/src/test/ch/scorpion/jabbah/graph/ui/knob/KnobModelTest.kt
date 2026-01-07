package ch.scorpion.jabbah.graph.ui.knob

import ch.scorpion.jabbah.base.math.PI_2
import ch.scorpion.jabbah.base.math.TWO_PI
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.math.PI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KnobModelTest {

    @BeforeTest
    fun setup() {
        GraphViewTestRule.configure()
    }

    @Test
    fun shouldCalculateAngle() {
        assertEquals(0.0, KnobModel(0).asAngle)
        assertEquals(0.0, KnobModel(1).asAngle)
        assertEquals(0.0, KnobModel(10).asAngle)

        assertEquals(PI / 2, KnobModel(325).asAngle)

        assertEquals(0.0, KnobModel(100).asAngle)
        assertEquals(PI, KnobModel(550).asAngle)
    }

    @Test
    fun shouldDragToAngle() {
        assertEquals(2L, KnobModel(1).incrementAngleTo(TWO_PI / 9))
        assertEquals(6L, KnobModel(1).incrementAngleTo(5 * TWO_PI / 9))

        assertEquals(20_000L, KnobModel(10_000).incrementAngleTo(TWO_PI / 9))
        assertEquals(60_000L, KnobModel(10_000).incrementAngleTo(5 * TWO_PI / 9))
    }

    @Test
    fun shouldIncrementAngleAcrossOrigin() {
        assertEquals(3_250L, KnobModel(999).incrementAngleTo(PI_2))
    }
}