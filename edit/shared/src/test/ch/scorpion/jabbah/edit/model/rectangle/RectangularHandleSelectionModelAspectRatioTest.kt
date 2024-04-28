package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.model.ImageRepositoryMockBuilder
import ch.scorpion.jabbah.edit.model.image.ImageComponent
import ch.scorpion.jabbah.edit.module.EditModule
import kotlin.test.Test
import kotlin.test.assertEquals

class RectangularHandleSelectionModelAspectRatioTest : AbstractRectangularHandleSelectionModelTest() {

    init {
        EditModule.imageRepository = ImageRepositoryMockBuilder()
            .withImageOfSize(200, 100)
            .build()
    }

    @Test
    fun shouldDragSouthEast() {
        val rect = addSelectedRect(0, 0, 200, 100)

        driver.pressAndDragTo(200, 100, 222, 150)

        assertEquals(Point2D.Companion.ZERO, rect.location)
        assertEquals(300.0, rect.width)
        assertEquals(150.0, rect.height)
    }

    @Test
    fun shouldDragSouthWest() {
        val rect = addSelectedRect(0, 0, 200, 100)

        driver.pressAndDragTo(0, 100, -22, 150)

        assertEquals(Point2D(-100, 0), rect.location)
        assertEquals(300.0, rect.width)
        assertEquals(150.0, rect.height)
    }

    @Test
    fun shouldDragNorthEast() {
        val rect = addSelectedRect(0, 0, 200, 100)

        driver.pressAndDragTo(200, 0, 300, 10)

        assertEquals(Point2D(0, -50), rect.location)
        assertEquals(300.0, rect.width)
        assertEquals(150.0, rect.height)
    }

    @Test
    fun shouldDragNorthWest() {
        val rect = addSelectedRect(0, 0, 200, 100)

        driver.pressAndDragTo(0, 0, -100, 10)

        assertEquals(Point2D(-100, -50), rect.location)
        assertEquals(300.0, rect.width)
        assertEquals(150.0, rect.height)
    }

    override fun createRect(x: Int, y: Int, width: Int, height: Int): RectangleComponent {
        // Dimension not used, determined by ImageRepository mock
        val rect = ImageComponent(System.createUUID())
        rect.location = Point2D(x, y)
        return rect
    }
}