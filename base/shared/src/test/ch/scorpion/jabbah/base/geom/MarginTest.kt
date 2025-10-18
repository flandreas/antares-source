package ch.scorpion.jabbah.base.geom

import kotlin.test.Test
import kotlin.test.assertEquals

class MarginTest {

    @Test
    fun shouldReduceRectangle() {
        val rect = Rectangle2D(0, 0, 200, 100)
        assertEquals(Rectangle2D(10, 10, 180, 80), Margin.allOf(10).reduce(rect))
    }

    @Test
    fun shouldNotReduceRectangleToNegativeWidth() {
        val rect = Rectangle2D(0, 0, 100, 200)
        assertEquals(Rectangle2D(60, 60, 0, 80), Margin.allOf(60).reduce(rect))
    }

    @Test
    fun shouldNotReduceRectangleToNegativeHeight() {
        val rect = Rectangle2D(0, 0, 200, 100)
        assertEquals(Rectangle2D(60, 60, 80, 0), Margin.allOf(60).reduce(rect))
    }
}