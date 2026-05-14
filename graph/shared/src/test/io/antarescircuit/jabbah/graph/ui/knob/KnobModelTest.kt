package io.antarescircuit.jabbah.graph.ui.knob

import io.antarescircuit.jabbah.base.math.PI_2
import io.antarescircuit.jabbah.base.math.TWO_PI
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude.*
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.Factor
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit.Volt
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import kotlin.math.PI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KnobModelTest {

    @BeforeTest
    fun setup() {
        GraphViewTestRule.configure()
    }

    @Test
    fun shouldCalculateAngle() {
        assertEquals(0.0, KnobModel(MagnitudeValue(1, One, Factor)).asAngle)
        assertEquals(0.0, KnobModel(MagnitudeValue(10, One, Factor)).asAngle)

        assertEquals(PI / 2, KnobModel(MagnitudeValue(325, One, Factor)).asAngle)

        assertEquals(0.0, KnobModel(MagnitudeValue(100, One, Factor)).asAngle)
        assertEquals(PI, KnobModel(MagnitudeValue(550, One, Factor)).asAngle)
    }

    @Test
    fun shouldCalculateAngleWithMillis() {
        val value = MagnitudeValue(200, Milli, SIUnit.Second)
        val model = KnobModel(value)
        val angle = model.asAngle
        assertTrue(angle > 0)
        assertTrue(angle < PI_2)
    }

    @Test
    fun shouldDragToAngle() {
        assertEquals(MagnitudeValue(2, One, Factor), KnobModel(MagnitudeValue(1, One, Factor)).incrementAngleTo(TWO_PI / 9, false))
        assertEquals(MagnitudeValue(6, One, Factor), KnobModel(MagnitudeValue(1, One, Factor)).incrementAngleTo(5 * TWO_PI / 9, false))

        assertEquals(MagnitudeValue(20, Kilo, Factor), KnobModel(MagnitudeValue(10, Kilo, Factor)).incrementAngleTo(TWO_PI / 9, false))
        assertEquals(MagnitudeValue(60, Kilo, Factor), KnobModel(MagnitudeValue(10, Kilo, Factor)).incrementAngleTo(5 * TWO_PI / 9, false))
    }

    @Test
    fun shouldIncrementAngleAcrossOrigin() {
        assertEquals(MagnitudeValue(3.25, Kilo, Factor), KnobModel(MagnitudeValue(999, One, Factor)).incrementAngleTo(PI_2, true))
    }

    @Test
    fun shouldDecrementAngleAcrossOrigin() {
        assertEquals(MagnitudeValue(899, Milli, Volt), KnobModel(MagnitudeValue(2, One, Volt)).dragToAngle(
            8 * TWO_PI / 9, increment = false, changeMagnitude = true, snap = false))
    }
}