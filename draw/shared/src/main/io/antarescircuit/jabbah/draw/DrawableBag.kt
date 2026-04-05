package io.antarescircuit.jabbah.draw

import io.antarescircuit.jabbah.base.collection.ImmutableList
import io.antarescircuit.jabbah.base.collection.toImmutableList
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.base.geom.Rotation

/**
 * A lightweight version of [DrawableContainer] without the burden of extending [Drawable] and
 * maintaining [DrawableContainerListener]s.
 */
interface DrawableBag<T: Drawable> {

	val useLocation: Boolean

	/**
	 * Holds the [Drawable]s that this [DrawableBag] contains. The topmost [Drawable] is stored
	 * at the first position of the list.
	 */
	val drawables: List<T>

	/** Holds the location for relocating [InputEventContext] when forwarding events to the inner [Drawable]s.*/
	var location: Point2D

	var rotation: Rotation

	/** Returns the [Drawable] at the specified index, starting with zero.*/
	fun get(index: Int): T = drawables[index]

	/** Determines whether this [DrawableBag] contains the specified [Drawable].*/
	fun contains(drawable: Drawable): Boolean = drawables.contains(drawable)

	/** Determines whether any of this [DrawableBag]'s [Drawable]s contains the specified location. */
	fun contains(x: Double, y: Double): Boolean {
		if (useLocation) {
			val location = rotateBack(x, y).subtract(this.location)
			return drawables.any { it.visible && it.contains(location) }
		}
		return drawables.any { it.visible && it.contains(rotateBack(x, y)) }
	}

	/**
	 * Removes all [Drawable]s from this [DrawableBag].
	 * @return this [DrawableBag] to support method chaining
	 */
	fun clear(): DrawableBag<T>

	/**
	 * Adds the specified [Drawable] to this [DrawableContainer] at the specified stacking order index.
	 * @return this [DrawableContainer] to support method chaining
	 */
	fun add(drawable: T, index: Int = 0): DrawableBag<T>

	/**
	 * Removes the specified [Drawable] from this [DrawableContainer].
	 * @return this [DrawableContainer] to support method chaining
	 */
	fun remove(drawable: Drawable): DrawableBag<T>

	/** Returns an [Iterator] over all [Drawable]s in stacking order, i.e. the topmost [Drawable] is returned first.*/
	fun frontToBackIterator(): Iterator<T> = drawables.iterator()

	/** Returns an [Iterator] over all [Drawable]s in reverse stacking order, i.e. the bottommost [Drawable] is returned first.*/
	fun backToFrontIterator(): Iterator<T> {
		val iter = drawables.listIterator(drawables.size)
		return object : Iterator<T> {
			override fun hasNext(): Boolean = iter.hasPrevious()
			override fun next(): T = iter.previous()
		}
	}

	/**
	 * Returns the first visible [Drawable] (in stacking order) that contains the specified location,
	 * expressed relative to the environment's coordinate system origin (either a [View] or a parent [DrawableBag]).
	 * If the [Drawable] at the specified location is a [DrawableBag], this method must **not** return any
	 * inner [Drawable] at that location. This method rather returns only direct children.
	 *
	 * TODO: Providing a default value for [predicate] wouldn't compile in JS due to KT-41006 (due for Kotlin 1.4.20).
	 */
	fun getDrawableAt(x: Double, y: Double, predicate: (T) -> Boolean): T?  {
		if (useLocation) {
			val p = rotateBack(x, y).subtract(location)
			return drawables.firstOrNull { it.visible && predicate.invoke(it) && it.contains(p) }
		}
		val p = rotateBack(x, y)
		return drawables.firstOrNull { it.visible && predicate.invoke(it) && it.contains(p) }
	}

	fun getDrawableAt(rect: RectangularShape, predicate: (T) -> Boolean): T? {
		if (useLocation) {
			val r = rotateBack(rect).moveBy(location.negate)
			return drawables.firstOrNull { it.visible && predicate.invoke(it) && it.intersects(r) }
		}
		val r = rotateBack(rect)
		return drawables.firstOrNull { it.visible && predicate.invoke(it) && it.intersects(r) }
	}

	fun getDrawableAt(x: Double, y: Double): T? = getDrawableAt(x, y) { true }

	/** Delegates to [getDrawableAt] using the individual coordinates of the specified [Point2D]. */
	fun getDrawableAt(location: Point2D): T? = getDrawableAt(location.x, location.y) { true }

	/** Returns the [Drawable]s that match the specified predicate.*/
	fun getDrawables(predicate: (T) -> Boolean): ImmutableList<T> = drawables.filter(predicate).toImmutableList()

	fun getDrawableIntersection(drawable: Drawable, predicate: (T) -> Boolean = { true} ): Collection<T> {
		if (useLocation) {
			val r = rotateBack(drawable.boundingBox).moveBy(location.negate)
			return drawables.filter { it !== drawable && it.visible && predicate.invoke(it) && it.intersects(r) }
		}
		val r = rotateBack(drawable.boundingBox)
		return drawables.filter { it !== drawable && it.visible && predicate.invoke(it) && it.intersects(r) }
	}

	/** Returns the first [Drawable] that matches the specified predicate.*/
	fun getDrawable(predicate: (T) -> Boolean): T? = drawables.firstOrNull(predicate)

	fun rotateBack(x: Double, y: Double): Point2D
		= rotation.inverse().rotatePointAround(location, x, y)

	fun rotateBack(pos: Point2D): Point2D = rotateBack(pos.x, pos.y)

	fun rotateBack(rect: RectangularShape): Rectangle2D
		= rotation.inverse().rotateRectangleAround(location, rect)
}

inline fun <reified R> DrawableBag<*>.getDrawableInstances(): ImmutableList<R> =
	drawables.filterIsInstance<R>().toImmutableList()