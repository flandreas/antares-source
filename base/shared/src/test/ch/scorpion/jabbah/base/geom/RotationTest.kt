package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.`sameInstance`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [Rotation].
 */
class RotationTest {

    @Before
    fun setup() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldRetrieveByName() {
        assertThat(Rotation.withName("180"), `is`(`sameInstance`(Rotation.R180)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldNotRetrieveByUnknownName() {
        Rotation.withName("720")
    }

    @Test
    fun shouldCalculateNextRotation() {
        assertThat(Rotation.R0.next(), `is`(Rotation.R90))
        assertThat(Rotation.R90.next(), `is`(Rotation.R180))
        assertThat(Rotation.R180.next(), `is`(Rotation.R270))
        assertThat(Rotation.R270.next(), `is`(Rotation.R0))
    }

    @Test
    fun shouldCalculateOppositeRotation() {
        assertThat(Rotation.R0.opposite(), `is`(Rotation.R180))
        assertThat(Rotation.R90.opposite(), `is`(Rotation.R270))
        assertThat(Rotation.R180.opposite(), `is`(Rotation.R0))
        assertThat(Rotation.R270.opposite(), `is`(Rotation.R90))
    }

    @Test
    fun shouldRotatePoint() {
        assertThat(Rotation.R0.rotatePoint(4.0, 1.0), `is`(Point2D(4.0, 1.0)))
        assertThat(Rotation.R0.rotatePoint(1.0, -4.0), `is`(Point2D(1.0, -4.0)))
        assertThat(Rotation.R0.rotatePoint(-4.0, -1.0), `is`(Point2D(-4.0, -1.0)))
        assertThat(Rotation.R0.rotatePoint(-1.0, 4.0), `is`(Point2D(-1.0, 4.0)))

        assertThat(Rotation.R90.rotatePoint(4.0, 1.0), `is`(Point2D(1.0, -4.0)))
        assertThat(Rotation.R90.rotatePoint(1.0, -4.0), `is`(Point2D(-4.0, -1.0)))
        assertThat(Rotation.R90.rotatePoint(-4.0, -1.0), `is`(Point2D(-1.0, 4.0)))
        assertThat(Rotation.R90.rotatePoint(-1.0, 4.0), `is`(Point2D(4.0, 1.0)))

        assertThat(Rotation.R180.rotatePoint(4.0, 1.0), `is`(Point2D(-4.0, -1.0)))
        assertThat(Rotation.R180.rotatePoint(1.0, -4.0), `is`(Point2D(-1.0, 4.0)))
        assertThat(Rotation.R180.rotatePoint(-4.0, -1.0), `is`(Point2D(4.0, 1.0)))
        assertThat(Rotation.R180.rotatePoint(-1.0, 4.0), `is`(Point2D(1.0, -4.0)))

        assertThat(Rotation.R270.rotatePoint(4.0, 1.0), `is`(Point2D(-1.0, 4.0)))
        assertThat(Rotation.R270.rotatePoint(1.0, -4.0), `is`(Point2D(4.0, 1.0)))
        assertThat(Rotation.R270.rotatePoint(-4.0, -1.0), `is`(Point2D(1.0, -4.0)))
        assertThat(Rotation.R270.rotatePoint(-1.0, 4.0), `is`(Point2D(-4.0, -1.0)))
    }

    @Test
    fun shouldRotateRectangleAroundPivot() {
        val pivot = Point2D(2, -1)
        val rect = Rectangle2D(2, -3, 4, 2)
        assertThat(Rotation.R0.rotateRectangleAround(pivot, rect), `is`(Rectangle2D(2, -3, 4, 2)))
        assertThat(Rotation.R90.rotateRectangleAround(pivot, rect), `is`(Rectangle2D(0, -5, 2, 4)))
        assertThat(Rotation.R180.rotateRectangleAround(pivot, rect), `is`(Rectangle2D(-2, -1, 4, 2)))
        assertThat(Rotation.R270.rotateRectangleAround(pivot, rect), `is`(Rectangle2D(2, -1, 2, 4)))
    }

    @Test
    fun shouldRotateDirection() {
        assertThat(Rotation.R0.rotateDirection(Direction.EAST), `is`(Direction.EAST))
        assertThat(Rotation.R90.rotateDirection(Direction.EAST), `is`(Direction.NORTH))
        assertThat(Rotation.R180.rotateDirection(Direction.EAST), `is`(Direction.WEST))
        assertThat(Rotation.R270.rotateDirection(Direction.EAST), `is`(Direction.SOUTH))

        assertThat(Rotation.R0.rotateDirection(Direction.NORTH), `is`(Direction.NORTH))
        assertThat(Rotation.R90.rotateDirection(Direction.NORTH), `is`(Direction.WEST))
        assertThat(Rotation.R180.rotateDirection(Direction.NORTH), `is`(Direction.SOUTH))
        assertThat(Rotation.R270.rotateDirection(Direction.NORTH), `is`(Direction.EAST))
    }
}