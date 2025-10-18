package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.geom.AbstractRectangularShape.Companion.OUT_BOTTOM
import ch.scorpion.jabbah.base.geom.AbstractRectangularShape.Companion.OUT_LEFT
import ch.scorpion.jabbah.base.geom.AbstractRectangularShape.Companion.OUT_RIGHT
import ch.scorpion.jabbah.base.geom.AbstractRectangularShape.Companion.OUT_TOP
import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.*

/** Unit tests for [Rectangle2D].*/
class Rectangle2DTest {

	@BeforeTest
	fun init() {
		BaseModule.require()
	}

	@Test
	fun shouldConstructWithPoint() {
		val rect = Rectangle2D(Point2D(10.0, 20.0), 30.0, 40.0)
		assertEquals(10.0, rect.x)
		assertEquals(20.0, rect.y)
	}

	@Test
	fun shouldCopy() {
		val rect = Rectangle2D(10.0, 20.0, 30.0, 40.0)
		val copy = rect.copy()
		copy.setFrame(100.0, 100.0, 200.0, 100.0)
		assertEquals(Rectangle2D(10.0, 20.0, 30.0, 40.0), rect)
		assertEquals(Rectangle2D(100.0, 100.0, 200.0, 100.0), copy)
	}

	@Test
	fun shouldReturnMinMax() {
		assertEquals(10.0, Rectangle2D(10.0, 20.0, 30.0, 40.0).minX)
		assertEquals(20.0, Rectangle2D(10.0, 20.0, 30.0, 40.0).minY)
		assertEquals(40.0, Rectangle2D(10.0, 20.0, 30.0, 40.0).maxX)
		assertEquals(60.0, Rectangle2D(10.0, 20.0, 30.0, 40.0).maxY)
	}

	@Test
	fun shouldSetFrame() {
		val rect = Rectangle2D(0.0, 0.0, 200.0, 100.0)
		rect.setFrame(10.0, 20.0, 1000.0, 2000.0)
		assertEquals(10.0, rect.x)
		assertEquals(20.0, rect.y)
		assertEquals(1000.0, rect.width)
		assertEquals(2000.0, rect.height)
	}

	@Test
	fun shouldSetFrameFromRect() {
		val rect = Rectangle2D(0.0, 0.0, 200.0, 100.0)
		rect.setFrame(Rectangle2D(10.0, 20.0, 1000.0, 2000.0))
		assertEquals(10.0, rect.x)
		assertEquals(20.0, rect.y)
		assertEquals(1000.0, rect.width)
		assertEquals(2000.0, rect.height)
	}

	@Test
	fun shouldContainLocation() {
		val rect = Rectangle2D(0.0, 0.0, 100.0, 100.0)
		assertTrue(rect.contains(0.0, 0.0))
		assertTrue(rect.contains(100.0, 100.0))
		assertTrue(rect.contains(50.0, 50.0))
	}

	@Test
	fun shouldNotContainLocation() {
		val rect = Rectangle2D(0.0, 0.0, 100.0, 100.0)
		assertFalse(rect.contains(-1.0, -1.0))
		assertFalse(rect.contains(101.0, 100.0))
		assertFalse(rect.contains(200.0, 200.0))
	}

	@Test
	fun shouldBeEmpty() {
		assertTrue(Rectangle2D(0.0, 0.0, 0.0, 0.0).isEmpty)
		assertFalse(Rectangle2D(0.0, 0.0, 100.0, 0.0).isEmpty)
		assertFalse(Rectangle2D(0.0, 0.0, 100.0, 100.0).isEmpty)
	}

	@Test
	fun shouldContainRectangle() {
		val rect = Rectangle2D(0.0, 0.0, 100.0, 100.0)
		assertTrue(rect.contains(0.0, 0.0, 10.0, 10.0))
		assertTrue(rect.contains(0.0, 0.0, 100.0, 100.0))
		assertTrue(rect.contains(10.0, 10.0, 10.0, 10.0))
		assertFalse(rect.contains(-1.0, 1.0, 100.0, 100.0))
		assertFalse(rect.contains(0.0, 0.0, 200.0, 200.0))
		assertFalse(rect.contains(200.0, 200.0, 100.0, 100.0))
	}

	@Test
	fun shouldContainFlatRectangle() {
		val rect = Rectangle2D(0.0, 0.0, 100.0, 100.0)
		assertTrue(rect.contains(10.0, 10.0, 40.0, 0.0))
	}

	@Test
	fun shouldAddLocation() {
		assertTrue(Rectangle2D(0.0, 0.0, 100.0, 100.0).add(200.0, 200.0).contains(200.0, 200.0))
	}

	@Test
	fun shouldAddNegativeLocation() {
		val rect = Rectangle2D()
		rect.add(0, -14)
		rect.add(29, 3)

		assertEquals(Rectangle2D(0, -14, 29, 17), rect)
	}

	@Test
	fun shouldAddRectangle() {
		val rect = Rectangle2D(0.0, 0.0, 100.0, 100.0).add(Rectangle2D(50.0, 50.0, 100.0, 100.0))
		assertEquals(Rectangle2D(0.0, 0.0, 150.0, 150.0), rect)
	}

	@Test
	fun shouldOutcode() {
		assertEquals(OUT_TOP, Rectangle2D(100, 100, 100, 100).outcode(150.0, 0.0))
		assertEquals(OUT_TOP or OUT_RIGHT, Rectangle2D(100, 100, 100, 100).outcode(300.0, 0.0))
		assertEquals(OUT_RIGHT, Rectangle2D(100, 100, 100, 100).outcode(300.0, 150.0))
		assertEquals(OUT_BOTTOM or OUT_RIGHT, Rectangle2D(100, 100, 100, 100).outcode(300.0, 300.0))
		assertEquals(OUT_BOTTOM, Rectangle2D(100, 100, 100, 100).outcode(150.0, 300.0))
		assertEquals(OUT_BOTTOM or OUT_LEFT, Rectangle2D(100, 100, 100, 100).outcode(0.0, 300.0))
		assertEquals(OUT_LEFT, Rectangle2D(100, 100, 100, 100).outcode(0.0, 150.0))
		assertEquals(OUT_TOP or OUT_LEFT, Rectangle2D(100, 100, 100, 100).outcode(0.0, 0.0))
	}

	@Test
	fun shouldIntersectLine() {
		val rect = Rectangle2D(100, 100, 100, 100)
		assertTrue(rect.intersectsLine(0.0, 150.0, 150.0, 150.0))
		assertTrue(rect.intersectsLine(150.0, 0.0, 150.0, 150.0))
		assertTrue(rect.intersectsLine(300.0, 150.0, 150.0, 150.0))
		assertTrue(rect.intersectsLine(150.0, 300.0, 150.0, 150.0))
	}

	@Test
	fun shouldNotIntersectLine() {
		val rect = Rectangle2D(100, 100, 100, 100)
		assertFalse(rect.intersectsLine(0.0, 0.0, 300.0, 0.0))
		assertFalse(rect.intersectsLine(150.0, 0.0, 300.0, 0.0))
	}

	@Test
	fun shouldAddFirstPoint() {
		val rect = Rectangle2D()
		rect.add(100, 100)
		assertEquals(Rectangle2D(100, 100, 0, 0), rect.boundingBox as Rectangle2D)
	}

	@Test
	fun shouldExpandLeft() {
		val rect = Rectangle2D(100, 100, 100, 100)
		rect.expandLeftBy(10.0)
		assertEquals(Rectangle2D(90, 100, 110, 100), rect)
	}

	@Test
	fun shouldExpandInward() {
		val rect = Rectangle2D(100, 100, 100, 100)
		rect.expandBy(-10.0)
		assertEquals(Rectangle2D(110, 110, 80, 80), rect)
	}

	@Test
	fun shouldAddPathWithClose() {
		val rect = Rectangle2D()
		rect.add(0, 0)
		rect.add(-50, -50)
		rect.add(-100, -50)
		rect.add(-100, 50)
		rect.add(-50, 50)
		rect.add(0, 0)

		assertEquals(Rectangle2D(-100, -50, 100, 100), rect.boundingBox)
	}
}