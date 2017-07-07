package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [Direction].
 */
class DirectionTest {

    @Before
    fun setup() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldCalculateNext() {
        assertThat(Direction.EAST.next(), `is`(Direction.NORTH))
        assertThat(Direction.NORTH.next(), `is`(Direction.WEST))
        assertThat(Direction.WEST.next(), `is`(Direction.SOUTH))
        assertThat(Direction.SOUTH.next(), `is`(Direction.EAST))
    }

    @Test
    fun shouldCalculatePrevious() {
        assertThat(Direction.EAST.previous(), `is`(Direction.SOUTH))
        assertThat(Direction.NORTH.previous(), `is`(Direction.EAST))
        assertThat(Direction.WEST.previous(), `is`(Direction.NORTH))
        assertThat(Direction.SOUTH.previous(), `is`(Direction.WEST))
    }

    @Test
    fun shouldCalculateDirectionOfDxDy() {
        assertThat(Direction.of(1, 0), `is`(Direction.EAST))
        assertThat(Direction.of(0, -1), `is`(Direction.NORTH))
        assertThat(Direction.of(-1, 0), `is`(Direction.WEST))
        assertThat(Direction.of(0, 1), `is`(Direction.SOUTH))
    }

    @Test
    fun shouldCalculateDirectionBetweenPoints() {
        assertThat(Direction.of(Point2D(0, 0), Point2D(2, 0)), `is`(Direction.EAST))
        assertThat(Direction.of(Point2D(0, 0), Point2D(0, -1)), `is`(Direction.NORTH))
        assertThat(Direction.of(Point2D(0, 0), Point2D(-1, 0)), `is`(Direction.WEST))
        assertThat(Direction.of(Point2D(0, 0), Point2D(0, 1)), `is`(Direction.SOUTH))
    }

    @Test
    fun shouldCalculateDirectionOfRotation() {
        assertThat(Direction.of(Rotation.R0), `is`(Direction.EAST))
        assertThat(Direction.of(Rotation.R90), `is`(Direction.NORTH))
        assertThat(Direction.of(Rotation.R180), `is`(Direction.WEST))
        assertThat(Direction.of(Rotation.R270), `is`(Direction.SOUTH))
    }

    @Test
    fun shouldCalculateOppositeSet() {
        assertThat(Direction.oppositeSet(emptySet()), `is`(emptySet()))
        assertThat(Direction.oppositeSet(setOf(Direction.WEST, Direction.SOUTH)), `is`(setOf(Direction.EAST, Direction.NORTH)))
    }

    @Test
    fun shouldCalculateOpposite() {
        assertThat(Direction.EAST.opposite(), `is`(Direction.WEST))
        assertThat(Direction.NORTH.opposite(), `is`(Direction.SOUTH))
        assertThat(Direction.WEST.opposite(), `is`(Direction.EAST))
        assertThat(Direction.SOUTH.opposite(), `is`(Direction.NORTH))
    }
}