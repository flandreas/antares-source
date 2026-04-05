package io.antarescircuit.jabbah.draw.container

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable

class QuadTree<T : Drawable>(
	val bounds: RectangularShape,
	private val parent: QuadTree<T>? = null,
	private val depth: Int = 1,
	private val capacity: Int = DEFAULT_CAPACITY,
	private val maxDepth: Int = DEFAULT_MAX_DEPTH
) {

	companion object {
		private const val DEFAULT_CAPACITY = 5
		const val DEFAULT_MAX_DEPTH = 5
	}

	/** The number of [Drawable]s directly contained in this [QuadTree], i.e. excluding the [Drawable] in the quadrants. */
	val size: Int get() = elements.size

	val isLeaf: Boolean get() = northWest == null

	val deepSize: Int get() {
		return if (isLeaf) {
			size
		} else {
			size + northWest!!.deepSize + northEast!!.deepSize + southWest!!.deepSize + southEast!!.deepSize
		}
	}

	/** The elements that are directly contained in this [QuadTree], i.e. don' fit into one of the quadrants. */
	private var elements = ArrayList<T>()

	// Visible for testing
	var northWest: QuadTree<T>? = null
		private set
	var northEast: QuadTree<T>? = null
		private set
	var southWest: QuadTree<T>? = null
		private set
	var southEast: QuadTree<T>? = null
		private set

	/** ---- Manipulation interface */

	fun add(drawable: T) {
		insert(drawable)
	}

	fun remove(drawable: T) {
		find(drawable)?.also { owner ->
			owner.elements.remove(drawable)
			if (owner.deepSize <= owner.capacity) {
				owner.compact()
			}
		}
	}

	/** ---- Query interface */

	fun contains(rect: Rectangle2D): Boolean = bounds.contains(rect)

	/** Finds the [Drawable]s that contain [p]. */
	fun findContains(p: Point2D): List<T> = ArrayList<T>().also { findContains(p, it) }

	/** Finds the [Drawable]s that intersect [rect]. */
	fun findIntersects(rect: Rectangle2D): List<T> = ArrayList<T>().also { findIntersect(rect, it) }

	fun drawGrid(context: DrawContext) {
		context.g.draw(bounds)
		if (!isLeaf) {
			northWest!!.drawGrid(context)
			northEast!!.drawGrid(context)
			southWest!!.drawGrid(context)
			southEast!!.drawGrid(context)
		}
	}

	/** ---- [QuadTree] */

	private fun insert(drawable: T): Boolean {
		if (!bounds.contains(drawable.boundingBox)) {
			return false
		}
		if (isLeaf && elements.size < capacity) {
			elements.add(0, drawable)
			return true
		}
		if (isLeaf && depth < maxDepth) {
			subdivide()
		}
		if (insertIntoQuadrant(drawable)) {
			return true
		}
		elements.add(0, drawable)
		return true
	}

	private fun insertIntoQuadrant(drawable: T): Boolean =
		!isLeaf && (
			northWest!!.insert(drawable)
			|| northEast!!.insert(drawable)
			|| southWest!!.insert(drawable)
			|| southEast!!.insert(drawable)
		)

	private fun subdivide() {
		val x = bounds.x
		val y = bounds.y
		val w2 = bounds.width / 2
		val h2 = bounds.height / 2

		northWest = QuadTree(Rectangle2D(x, y, w2, h2), parent = this, depth + 1, capacity, maxDepth)
		northEast = QuadTree(Rectangle2D(x + w2, y, w2, h2), parent = this, depth + 1, capacity, maxDepth)
		southWest = QuadTree(Rectangle2D(x, y + h2, w2, h2), parent = this, depth + 1, capacity, maxDepth)
		southEast = QuadTree(Rectangle2D(x + w2, y + h2, w2, h2), parent = this, depth + 1, capacity, maxDepth)

		val elementsToKeep = ArrayList<T>()
		for (e in elements) {
			if (!insertIntoQuadrant(e)) {
				elementsToKeep.add(e)
			}
		}

		elements = elementsToKeep
	}
	private fun compact() {
		val result = ArrayList<T>()
		collect(result)
		elements = result
		northWest = null
		northEast = null
		southWest = null
		southEast = null

		parent?.let {
			if (it.deepSize <= capacity) {
				it.compact()
			}
		}
	}

	/** Finds deeply all [Drawable]s containing [p] and returns the in [result]. */
	private fun findContains(p: Point2D, result: MutableList<T>) {
		if (!bounds.contains(p)) {
			return
		}
		elements.filter { it.boundingBox.contains(p) }.forEach { result.add(it) }
		if (isLeaf) {
			return
		}
		northWest!!.findContains(p, result)
		northEast!!.findContains(p, result)
		southWest!!.findContains(p, result)
		southEast!!.findContains(p, result)
	}

	/** Finds deeply all [Drawable]s that intersect [rect] and returns them in [result]. */
	private fun findIntersect(rect: Rectangle2D, result: MutableList<T>) {
		if (!rect.intersects(bounds)) {
			return
		}
		elements.filter { it.boundingBox.intersects(rect) }.forEach { result.add(it) }
		if (isLeaf) {
			return
		}
		northWest!!.findIntersect(rect, result)
		northEast!!.findIntersect(rect, result)
		southWest!!.findIntersect(rect, result)
		southEast!!.findIntersect(rect, result)
	}

	/** Finds the deepest [QuadTree] containing [drawable] as a direct element. */
	private fun find(drawable: T): QuadTree<T>? {
		if (!bounds.contains(drawable.boundingBox)) {
			return null
		}
		if (elements.contains(drawable)) {
			return this
		}
		if (isLeaf) {
			return null
		}

		northWest!!.find(drawable)?.let { return northWest }
		northEast!!.find(drawable)?.let { return northEast }
		southWest!!.find(drawable)?.let { return southWest }
		southEast!!.find(drawable)?.let { return southEast }

		return null
	}

	/** Collects deeply all [Drawable]s and returns them in [result]. */
	private fun collect(result: MutableList<T>) {
		result.addAll(elements)
		if (!isLeaf) {
			northWest!!.collect(result)
			northEast!!.collect(result)
			southWest!!.collect(result)
			southEast!!.collect(result)
		}
	}
}