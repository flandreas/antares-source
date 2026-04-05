package io.antarescircuit.jabbah.edit.model.rectangle

import io.antarescircuit.jabbah.base.geom.Point2D
import kotlin.test.Test
import kotlin.test.assertEquals

class RectangularHandleSelectionModelTest : AbstractRectangularHandleSelectionModelTest() {

    @Test
    fun shouldDragNorthWest() {
        val rect = addSelectedRect(0, 0, 200, 100)

        driver.pressAndDragTo(0, 0, -100, -50)

        assertEquals(Point2D(-100, -50), rect.location)
        assertEquals(300.0, rect.width)
        assertEquals(150.0, rect.height)
    }

    @Test
    fun shouldDragNorth() {
        val rect = addSelectedRect(0, 0, 200, 100)

        driver.pressAndDragTo(100, 0, 100, -50)

        assertEquals(Point2D(0, -50), rect.location)
        assertEquals(200.0, rect.width)
        assertEquals(150.0, rect.height)
    }

    @Test
    fun shouldDragNorthEast() {
        val rect = addSelectedRect(0, 0, 200, 100)

        driver.pressAndDragTo(200, 0, 250, -50)

        assertEquals(Point2D(0, -50), rect.location)
        assertEquals(250.0, rect.width)
        assertEquals(150.0, rect.height)
    }

    @Test
    fun shouldDragEast() {
        val rect = addSelectedRect(0, 0, 200, 100)

        driver.pressAndDragTo(200, 50, 250, 50)

        assertEquals(Point2D(0, 0), rect.location)
        assertEquals(250.0, rect.width)
        assertEquals(100.0, rect.height)
    }

    @Test
    fun shouldDragSouthEast() {
        val rect = addSelectedRect(0, 0, 200, 100)

        driver.pressAndDragTo(200, 100, 300, 150)

        assertEquals(Point2D.Companion.ZERO, rect.location)
        assertEquals(300.0, rect.width)
        assertEquals(150.0, rect.height)
    }

    @Test
    fun shouldDragSouth() {
        val rect = addSelectedRect(0, 0, 200, 100)

        driver.pressAndDragTo(100, 100, 100, 150)

        assertEquals(Point2D.Companion.ZERO, rect.location)
        assertEquals(200.0, rect.width)
        assertEquals(150.0, rect.height)
    }

    @Test
    fun shouldDragSouthWest() {
        val rect = addSelectedRect(0, 0, 200, 100)

        driver.pressAndDragTo(0, 100, -50, 150)

        assertEquals(Point2D(-50, 0), rect.location)
        assertEquals(250.0, rect.width)
        assertEquals(150.0, rect.height)
    }

    @Test
    fun shouldDragWest() {
        val rect = addSelectedRect(0, 0, 200, 100)

        driver.pressAndDragTo(0, 50, -50, 50)

        assertEquals(Point2D(-50, 0), rect.location)
        assertEquals(250.0, rect.width)
        assertEquals(100.0, rect.height)
    }
}