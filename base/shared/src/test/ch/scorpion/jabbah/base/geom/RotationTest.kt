package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.*

/** Unit tests for [Rotation].*/
class RotationTest {

    @BeforeTest
    fun setup() {
        BaseModule.require()
    }

    @Test
    fun shouldRetrieveByName() {
        assertSame(Rotation.R180, Rotation.withName("180"))
    }

    @Test
    fun shouldNotRetrieveByUnknownName() {
	    assertFailsWith<IllegalArgumentException> { Rotation.withName("720") }
    }

    @Test
    fun shouldCalculateNextRotation() {
        assertEquals(Rotation.R90, Rotation.R0.next())
        assertEquals(Rotation.R180, Rotation.R90.next())
        assertEquals(Rotation.R270, Rotation.R180.next())
        assertEquals(Rotation.R0, Rotation.R270.next())
    }

    @Test
    fun shouldCalculateOppositeRotation() {
        assertEquals(Rotation.R180, Rotation.R0.opposite())
        assertEquals(Rotation.R270, Rotation.R90.opposite())
        assertEquals(Rotation.R0, Rotation.R180.opposite())
        assertEquals(Rotation.R90, Rotation.R270.opposite())
    }

    @Test
    fun shouldRotatePoint() {
        assertEquals(Point2D(4.0, 1.0), Rotation.R0.rotatePoint(4.0, 1.0))
        assertEquals(Point2D(1.0, -4.0), Rotation.R0.rotatePoint(1.0, -4.0))
        assertEquals(Point2D(-4.0, -1.0), Rotation.R0.rotatePoint(-4.0, -1.0))
        assertEquals(Point2D(-1.0, 4.0), Rotation.R0.rotatePoint(-1.0, 4.0))

        assertEquals(Point2D(1.0, -4.0), Rotation.R90.rotatePoint(4.0, 1.0))
        assertEquals(Point2D(-4.0, -1.0), Rotation.R90.rotatePoint(1.0, -4.0))
        assertEquals(Point2D(-1.0, 4.0), Rotation.R90.rotatePoint(-4.0, -1.0))
        assertEquals(Point2D(4.0, 1.0), Rotation.R90.rotatePoint(-1.0, 4.0))

        assertEquals(Point2D(-4.0, -1.0), Rotation.R180.rotatePoint(4.0, 1.0))
        assertEquals(Point2D(-1.0, 4.0), Rotation.R180.rotatePoint(1.0, -4.0))
        assertEquals(Point2D(4.0, 1.0), Rotation.R180.rotatePoint(-4.0, -1.0))
        assertEquals(Point2D(1.0, -4.0), Rotation.R180.rotatePoint(-1.0, 4.0))

        assertEquals(Point2D(-1.0, 4.0), Rotation.R270.rotatePoint(4.0, 1.0))
        assertEquals(Point2D(4.0, 1.0), Rotation.R270.rotatePoint(1.0, -4.0))
        assertEquals(Point2D(1.0, -4.0), Rotation.R270.rotatePoint(-4.0, -1.0))
        assertEquals(Point2D(-4.0, -1.0), Rotation.R270.rotatePoint(-1.0, 4.0))
    }

    @Test
    fun shouldRotateRectangleAroundPivot() {
        val pivot = Point2D(2, -1)
        val rect = Rectangle2D(2, -3, 4, 2)
        assertEquals(Rectangle2D(2, -3, 4, 2), Rotation.R0.rotateRectangleAround(pivot, rect))
        assertEquals(Rectangle2D(0, -5, 2, 4), Rotation.R90.rotateRectangleAround(pivot, rect))
        assertEquals(Rectangle2D(-2, -1, 4, 2), Rotation.R180.rotateRectangleAround(pivot, rect))
        assertEquals(Rectangle2D(2, -1, 2, 4), Rotation.R270.rotateRectangleAround(pivot, rect))
    }

    @Test
    fun shouldRotateDirection() {
        assertEquals(Direction.EAST, Rotation.R0.rotateDirection(Direction.EAST))
        assertEquals(Direction.NORTH, Rotation.R90.rotateDirection(Direction.EAST))
        assertEquals(Direction.WEST, Rotation.R180.rotateDirection(Direction.EAST))
        assertEquals(Direction.SOUTH, Rotation.R270.rotateDirection(Direction.EAST))

        assertEquals(Direction.NORTH,Rotation.R0.rotateDirection(Direction.NORTH))
        assertEquals(Direction.WEST, Rotation.R90.rotateDirection(Direction.NORTH))
        assertEquals(Direction.SOUTH,Rotation.R180.rotateDirection(Direction.NORTH))
        assertEquals(Direction.EAST, Rotation.R270.rotateDirection(Direction.NORTH))
    }
}