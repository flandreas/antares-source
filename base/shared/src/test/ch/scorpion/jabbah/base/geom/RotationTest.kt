package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.geom.Direction.*
import ch.scorpion.jabbah.base.geom.Rotation.*
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
        assertSame(R180, Rotation.withName("180"))
    }

    @Test
    fun shouldNotRetrieveByUnknownName() {
	    assertFailsWith<IllegalArgumentException> { Rotation.withName("720") }
    }

    @Test
    fun shouldCalculateNextRotation() {
        assertEquals(R90, R0.next())
        assertEquals(R180, R90.next())
        assertEquals(R270, R180.next())
        assertEquals(R0, R270.next())
    }

	@Test
	fun shouldCalculatePreviousRotation() {
		assertEquals(R270, R0.previous())
		assertEquals(R0, R90.previous())
		assertEquals(R90, R180.previous())
		assertEquals(R180, R270.previous())
	}

    @Test
    fun shouldCalculateOppositeRotation() {
        assertEquals(R180, R0.opposite())
        assertEquals(R270, R90.opposite())
        assertEquals(R0, R180.opposite())
        assertEquals(R90, R270.opposite())
    }

    @Test
    fun shouldRotatePoint() {
        assertEquals(Point2D(4.0, 1.0), R0.rotatePoint(4.0, 1.0))
        assertEquals(Point2D(1.0, -4.0), R0.rotatePoint(1.0, -4.0))
        assertEquals(Point2D(-4.0, -1.0), R0.rotatePoint(-4.0, -1.0))
        assertEquals(Point2D(-1.0, 4.0), R0.rotatePoint(-1.0, 4.0))

        assertEquals(Point2D(1.0, -4.0), R90.rotatePoint(4.0, 1.0))
        assertEquals(Point2D(-4.0, -1.0), R90.rotatePoint(1.0, -4.0))
        assertEquals(Point2D(-1.0, 4.0), R90.rotatePoint(-4.0, -1.0))
        assertEquals(Point2D(4.0, 1.0), R90.rotatePoint(-1.0, 4.0))

        assertEquals(Point2D(-4.0, -1.0), R180.rotatePoint(4.0, 1.0))
        assertEquals(Point2D(-1.0, 4.0), R180.rotatePoint(1.0, -4.0))
        assertEquals(Point2D(4.0, 1.0), R180.rotatePoint(-4.0, -1.0))
        assertEquals(Point2D(1.0, -4.0), R180.rotatePoint(-1.0, 4.0))

        assertEquals(Point2D(-1.0, 4.0), R270.rotatePoint(4.0, 1.0))
        assertEquals(Point2D(4.0, 1.0), R270.rotatePoint(1.0, -4.0))
        assertEquals(Point2D(1.0, -4.0), R270.rotatePoint(-4.0, -1.0))
        assertEquals(Point2D(-4.0, -1.0), R270.rotatePoint(-1.0, 4.0))
    }

    @Test
    fun shouldRotateRectangleAroundPivot() {
        val pivot = Point2D(2, -1)
        val rect = Rectangle2D(2, -3, 4, 2)
        assertEquals(Rectangle2D(2, -3, 4, 2), R0.rotateRectangleAround(pivot, rect))
        assertEquals(Rectangle2D(0, -5, 2, 4), R90.rotateRectangleAround(pivot, rect))
        assertEquals(Rectangle2D(-2, -1, 4, 2), R180.rotateRectangleAround(pivot, rect))
        assertEquals(Rectangle2D(2, -1, 2, 4), R270.rotateRectangleAround(pivot, rect))
    }

    @Test
    fun shouldRotateDirection() {
        assertEquals(EAST, R0.rotateDirection(EAST))
        assertEquals(NORTH, R90.rotateDirection(EAST))
        assertEquals(WEST, R180.rotateDirection(EAST))
        assertEquals(SOUTH, R270.rotateDirection(EAST))

        assertEquals(NORTH, R0.rotateDirection(NORTH))
        assertEquals(WEST, R90.rotateDirection(NORTH))
        assertEquals(SOUTH, R180.rotateDirection(NORTH))
        assertEquals(EAST, R270.rotateDirection(NORTH))
    }

	@Test
	fun shouldAdd() {
		assertEquals(R90, R90.add(R0))
		assertEquals(R180, R90.add(R90))
		assertEquals(R90, R270.add(R180))
	}
}