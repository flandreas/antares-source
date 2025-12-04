package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Direction.EAST
import ch.scorpion.jabbah.base.geom.Direction.NORTH
import ch.scorpion.jabbah.base.geom.Direction.SOUTH
import ch.scorpion.jabbah.base.geom.Direction.WEST
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortOrEdgeConnector.Companion.calculateFreeNodeDirections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests of static members in [EdgeToPortOrEdgeConnector].
 */
class EdgeToPointOrEdgeConnectorTest {

    // Scenario "Incoming horizontal, outgoing vertical"

    @Test
    fun shouldCalculateFreeNodeDirectionsNE1() {
        val directions = calculateFreeNodeDirections(Point2D(100, 100), Point2D(0, 100), Point2D(100, 200))
        assertEquals(2, directions.size)
        assertTrue(directions.containsAll(setOf(NORTH, EAST)))
    }

    @Test
    fun shouldCalculateFreeNodeDirectionsSE1() {
        val directions = calculateFreeNodeDirections(Point2D(100, 100), Point2D(0, 100), Point2D(100, 0))
        assertEquals(2, directions.size)
        assertTrue(directions.containsAll(setOf(SOUTH, EAST)))
    }

    @Test
    fun shouldCalculateFreeNodeDirectionsSW1() {
        val directions = calculateFreeNodeDirections(Point2D(100, 100), Point2D(200, 100), Point2D(100, 0))
        assertEquals(2, directions.size)
        assertTrue(directions.containsAll(setOf(SOUTH, WEST)))
    }

    @Test
    fun shouldCalculateFreeNodeDirectionsNW1() {
        val directions = calculateFreeNodeDirections(Point2D(100, 100), Point2D(200, 100), Point2D(100, 200))
        assertEquals(2, directions.size)
        assertTrue(directions.containsAll(setOf(NORTH, WEST)))
    }

    // Scenario "Incoming vertical, outgoing horizontal"

    @Test
    fun shouldCalculateFreeNodeDirectionsNE2() {
        val directions = calculateFreeNodeDirections(Point2D(100, 100), Point2D(100, 200), Point2D(0, 100))
        assertEquals(2, directions.size)
        assertTrue(directions.containsAll(setOf(NORTH, EAST)))
    }

    @Test
    fun shouldCalculateFreeNodeDirectionsSE2() {
        val directions = calculateFreeNodeDirections(Point2D(100, 100), Point2D(100, 0), Point2D(0, 100))
        assertEquals(2, directions.size)
        assertTrue(directions.containsAll(setOf(SOUTH, EAST)))
    }

    @Test
    fun shouldCalculateFreeNodeDirectionsSW2() {
        val directions = calculateFreeNodeDirections(Point2D(100, 100), Point2D(100, 0), Point2D(200, 100))
        assertEquals(2, directions.size)
        assertTrue(directions.containsAll(setOf(SOUTH, WEST)))
    }

    @Test
    fun shouldCalculateFreeNodeDirectionsNW2() {
        val directions = calculateFreeNodeDirections(Point2D(100, 100), Point2D(100, 200), Point2D(200, 100))
        assertEquals(2, directions.size)
        assertTrue(directions.containsAll(setOf(NORTH, WEST)))
    }
}