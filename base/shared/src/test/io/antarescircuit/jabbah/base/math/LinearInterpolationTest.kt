package io.antarescircuit.jabbah.base.math

import kotlin.test.Test
import kotlin.test.assertEquals

class LinearInterpolationTest {

    @Test
    fun shouldInterpolateInt() {
        // Inside
        assertEquals(5, interpolateInt(50, 0, 0, 100, 10))
        assertEquals(0, interpolateInt(0, 0, 0, 100, 10))
        assertEquals(10, interpolateInt(100, 0, 0, 100, 10))

        // Outside
        assertEquals(-5, interpolateInt(-50, 0, 0, 100, 10))
        assertEquals(15, interpolateInt(150, 0, 0, 100, 10))

        // Horizontal
        assertEquals(0, interpolateInt(50, 0, 0, 100, 0))
    }

    @Test
    fun shouldInterpolateIntRange() {
        assertEquals(5, (0..100).interpolate(50, 0, 10))
    }
}