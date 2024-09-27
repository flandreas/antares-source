package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractDrawableTest.TestDrawable
import kotlin.test.*


@Ignore
class QuadTreeTest {

	@Test
	fun shouldSubdivideDisjunct() {
		val quadTree: QuadTree<Drawable> = QuadTree(Rectangle2D(0, 0, 100, 100), capacity = 1)

		quadTree.add(TestDrawable(10, 10, 10, 10)) // Northwest
		quadTree.add(TestDrawable(80, 10, 10, 10)) // Northeast

		assertEquals(0, quadTree.size)
		assertFalse(quadTree.isLeaf)
	}

	@Test
	fun shouldSubdivideRecursively() {
		val drawing = QuadrantDrawing()
		val d: Drawable = TestDrawable(30, 30, 10, 10)

		drawing.quadTree.add(d)

		assertFalse(drawing.quadTree.northWest!!.isLeaf)
		assertTrue(drawing.quadTree.northEast!!.isLeaf)
		assertTrue(drawing.quadTree.southWest!!.isLeaf)
		assertTrue(drawing.quadTree.southEast!!.isLeaf)
		assertEquals(Rectangle2D(0, 0, 25, 25), drawing.quadTree.northWest!!.northWest!!.bounds)
		assertEquals(Rectangle2D(25, 0, 25, 25), drawing.quadTree.northWest!!.northEast!!.bounds)
		assertEquals(Rectangle2D(0, 25, 25, 25), drawing.quadTree.northWest!!.southWest!!.bounds)
		assertEquals(Rectangle2D(25, 25, 25, 25), drawing.quadTree.northWest!!.southEast!!.bounds)
		assertTrue(drawing.quadTree.northWest!!.southEast!!.findContains(Point2D(35, 35)).contains(d))
	}

	@Test
	fun shouldNotExceedMaxDepth() {
		val d = QuadrantDrawing(maxDepth = 1)
		assertTrue(d.quadTree.isLeaf)
	}

	@Test
	fun shouldCalculateDeepSize() {
		val d = QuadrantDrawing()
		assertEquals(5, d.quadTree.deepSize)
	}

	@Test
	fun shouldCompact() {
		val d = QuadrantDrawing()

		d.quadTree.remove(d.all)
		assertEquals(4, d.quadTree.deepSize)
		assertFalse(d.quadTree.isLeaf)

		d.quadTree.remove(d.nw)
		assertEquals(3, d.quadTree.deepSize)
		assertFalse(d.quadTree.isLeaf)

		d.quadTree.remove(d.ne)
		assertEquals(2, d.quadTree.deepSize)
		assertFalse(d.quadTree.isLeaf)

		d.quadTree.remove(d.sw)
		assertEquals(1, d.quadTree.deepSize)
		assertTrue(d.quadTree.isLeaf)
	}

	@Test
	fun shouldContainPoint() {
		val d = QuadrantDrawing()
		assertTrue(d.quadTree.findContains(Point2D(15, 15)).containsAll(listOf(d.nw, d.all)))
		assertFalse(d.quadTree.findContains(Point2D(15, 15)).contains(d.ne))
		assertTrue(d.quadTree.findContains(Point2D(85, 15)).containsAll(listOf(d.ne, d.all)))
		assertTrue(d.quadTree.findContains(Point2D(15, 85)).containsAll(listOf(d.sw, d.all)))
		assertTrue(d.quadTree.findContains(Point2D(85, 85)).containsAll(listOf(d.se, d.all)))
	}

	@Test
	fun shouldIntersectRectangle() {
		val d = QuadrantDrawing()
		assertTrue(d.quadTree.findIntersects(Rectangle2D(0, 0, 50, 50)).containsAll(listOf(d.nw, d.all)))
		assertTrue(d.quadTree.findIntersects(Rectangle2D(50, 0, 50, 50)).containsAll(listOf(d.ne, d.all)))
		assertTrue(d.quadTree.findIntersects(Rectangle2D(0, 50, 50, 50)).containsAll(listOf(d.sw, d.all)))
		assertTrue(d.quadTree.findIntersects(Rectangle2D(50, 50, 50, 50)).containsAll(listOf(d.se, d.all)))
		assertTrue(d.quadTree.findIntersects(Rectangle2D(-100, -100, 300, 300)).containsAll(listOf(d.nw, d.ne, d.sw, d.se, d.all)))
	}

	private class QuadrantDrawing(maxDepth: Int = QuadTree.DEFAULT_MAX_DEPTH) {
		val quadTree = QuadTree<Drawable>(Rectangle2D(0, 0, 100, 100), capacity = 1, maxDepth = maxDepth)
		val nw = TestDrawable(10, 10, 10, 10)
		val ne = TestDrawable(80, 10, 10, 10)
		val sw = TestDrawable(10, 80, 10, 10)
		val se = TestDrawable(80, 80, 10, 10)
		val all = TestDrawable(10, 10, 80, 80)

		init {
			quadTree.apply {
				add(nw)
				add(ne)
				add(sw)
				add(se)
				add(all)
			}
		}
	}
}