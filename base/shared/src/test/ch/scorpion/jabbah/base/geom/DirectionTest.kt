package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.*

/**
 * Unit tests for [Direction].
 */
class DirectionTest {

	@BeforeTest
	fun setup() {
		BaseModule.require()
	}

	@Test
	fun shouldCalculateNext() {
		assertEquals(Direction.NORTH, Direction.EAST.next())
		assertEquals(Direction.WEST, Direction.NORTH.next())
		assertEquals(Direction.SOUTH, Direction.WEST.next())
		assertEquals(Direction.EAST, Direction.SOUTH.next())
	}

	@Test
	fun shouldCalculatePrevious() {
		assertEquals(Direction.SOUTH, Direction.EAST.previous())
		assertEquals(Direction.EAST, Direction.NORTH.previous())
		assertEquals(Direction.NORTH, Direction.WEST.previous())
		assertEquals(Direction.WEST, Direction.SOUTH.previous())
	}

	@Test
	fun shouldCalculateDirectionOfDxDy() {
		assertEquals(Direction.EAST, Direction.of(1, 0))
		assertEquals(Direction.NORTH, Direction.of(0, -1))
		assertEquals(Direction.WEST, Direction.of(-1, 0))
		assertEquals(Direction.SOUTH, Direction.of(0, 1))
	}

	@Test
	fun shouldCalculateDirectionBetweenPoints() {
		assertEquals(Direction.EAST, Direction.of(Point2D(0, 0), Point2D(2, 0)))
		assertEquals(Direction.NORTH, Direction.of(Point2D(0, 0), Point2D(0, -1)))
		assertEquals(Direction.WEST, Direction.of(Point2D(0, 0), Point2D(-1, 0)))
		assertEquals(Direction.SOUTH, Direction.of(Point2D(0, 0), Point2D(0, 1)))
	}

	@Test
	fun shouldCalculateDirectionOfRotation() {
		assertEquals(Direction.EAST, Direction.of(Rotation.R0))
		assertEquals(Direction.NORTH, Direction.of(Rotation.R90))
		assertEquals(Direction.WEST, Direction.of(Rotation.R180))
		assertEquals(Direction.SOUTH, Direction.of(Rotation.R270))
	}

	@Test
	fun shouldCalculateOppositeSet() {
		assertEquals(emptySet(), Direction.oppositeSet(emptySet()))
		assertEquals(setOf(Direction.EAST, Direction.NORTH), Direction.oppositeSet(setOf(Direction.WEST, Direction.SOUTH)))
	}

	@Test
	fun shouldCalculateOpposite() {
		assertEquals(Direction.WEST, Direction.EAST.opposite())
		assertEquals(Direction.SOUTH, Direction.NORTH.opposite())
		assertEquals(Direction.EAST, Direction.WEST.opposite())
		assertEquals(Direction.NORTH, Direction.SOUTH.opposite())
	}

	@Test
	fun shouldBeHorizontal() {
		assertTrue(Direction.EAST.isHorizontal())
		assertTrue(Direction.WEST.isHorizontal())
	}

	@Test
	fun shouldNotBeHorizontal() {
		assertFalse(Direction.NORTH.isHorizontal())
		assertFalse(Direction.SOUTH.isHorizontal())
	}

	@Test
	fun shouldBeVertical() {
		assertTrue(Direction.NORTH.isVertical())
		assertTrue(Direction.SOUTH.isVertical())
	}

	@Test
	fun shouldNotBeVertical() {
		assertFalse(Direction.WEST.isVertical())
		assertFalse(Direction.EAST.isVertical())
	}

	@Test
	fun shouldAvoidRoundingIssues() {
		assertEquals(Direction.EAST, Direction.of(Point2D(-224.0, -111.999999999), Point2D(10.0, -112.0)))
	}

	@Test
	fun shouldAcceptFloats() {
		assertEquals(Direction.WEST, Direction.of(Point2D(-364.0, -294.0), Point2D(-364.5, -294.0)))
	}
}