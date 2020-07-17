package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.drawable.Locatable

/**
 * A [DrawableContainer] is a [Drawable] that contains other [Drawable]s.
 *
 * A [Drawable] cannot be contained in more than one [DrawableContainer]. For applications that must display
 * the same model information with different graphical representations, consider splitting model and view
 * ([Drawable]) classes. But there is nothing wrong with showing the same [Drawable] objects in multiple
 * panels, for example with different zoom or pan factors.
 *
 * A [DrawableContainer] doesn't register itself as [DrawableListener] on every [Drawable] it contains.
 * It rather expects that a [Drawable] maintains a reference to its parent [DrawableContainer] and calls
 * the appropriate handle()-methods of its parent [DrawableContainer] whenever needed. This design allows
 * domain logic to search for other [Drawable]s in a particular [Drawable]'s [DrawableContainer].
 *
 * Coordinates used by [DrawableContainer] methods are expressed relative to the origin of the coordinate system
 * of the [View] that displays this [DrawableContainer], or in the coordinate system of the [DrawableContainer]
 * that contains this [DrawableContainer] as a child [Drawable]. This ensures semantic consistency of the
 * [DrawableContainer] methods and those inherited from [Drawable].
 *
 * @param T the type of [Drawable]s that this [DrawableContainer] contains
 */
interface DrawableContainer<T : Drawable> : Drawable, Locatable {

    /** Returns the number of [Drawable]s this [DrawableContainer] contains.*/
    val drawablesCount: Int

    /**
     * Sets the [DrawableDrawer] to be used for drawing the [Drawable]s of this [DrawableContainer] and
     * thus replacing the existing chain of [DrawableDrawer]s.
     */
    fun setDrawableDrawer(drawableDrawer: DrawableDrawer<T>)

    /** Adds the specified [DrawableDrawer] at the head of the chain of [DrawableDrawer]s. */
    fun addDrawableDrawer(drawableDrawer: DrawableDrawer<T>)

    /**
     * Add the specified [DrawableContainerListener] to listen for [DrawableContainerEvent]s
     * from this [DrawableContainer].
     */
    fun addDrawableContainerListener(listener: DrawableContainerListener<T>)

    /**
     * Removes the specified [DrawableContainerListener] to stop listening for [DrawableContainerEvent]s
     * from this [DrawableContainer].
     */
    fun removeDrawableContainerListener(listener: DrawableContainerListener<T>)

    /** Returns the [Drawable] at the specified index, starting with zero.*/
    fun get(index: Int): T

    /** Determines whether this [DrawableContainer] contains the specified [Drawable].*/
    fun contains(drawable: Drawable): Boolean

    /**
     * Adds the specified [Drawable] to this [DrawableContainer] at the front of the stacking order.
     * Calls [DrawableContainerListener.drawableAdded] on all registered listeners.
     * @return this [DrawableContainer] to support method chaining
     */
    fun add(drawable: T): DrawableContainer<T>

    /**
     * Adds the specified [Drawable] to this [DrawableContainer] at the specified stacking order index.
     * Calls [DrawableContainerListener.drawableAdded] on all registered listeners.
     * @return this [DrawableContainer] to support method chaining
     */
    fun add(drawable: T, index: Int): DrawableContainer<T>

    /**
     * Removes the specified [Drawable] from this [DrawableContainer].
     * Calls [DrawableContainerListener.drawableRemoved] on all registered listeners.
     * @return this [DrawableContainer] to support method chaining
     */
    fun remove(drawable: Drawable): DrawableContainer<T>

    /**
     * Removes all [Drawable]s from this [DrawableContainer].
     * Calls [DrawableContainerListener.drawableRemoved] on all registered listeners for all removed [Drawable]s.
     * @return this [DrawableContainer] to support method chaining
     */
    fun clear(): DrawableContainer<T>

    /** Returns an [Iterator] over all [Drawable]s in stacking order, i.e. the topmost [Drawable] is returned first.*/
    fun frontToBackIterator(): Iterator<T>

    /** Returns an [Iterator] over all [Drawable]s in reverse stacking order, i.e. the bottommost [Drawable] is returned first.*/
    fun backToFrontIterator(): Iterator<T>

    /**
     * Returns the first visible [Drawable] (in stacking order) that contains the specified location,
     * expressed relative to the environment's coordinate system origin (either a [View] or a parent [DrawableContainer]).
     * If the [Drawable] at the specified location is a [DrawableContainer], this method must **not** return any
     * inner [Drawable] at that location. This method rather returns only direct children.
     */
    fun getDrawableAt(x: Double, y: Double): T?

	/** Delegates to [getDrawableAt] using the individual coordinates of the specified [Point2D]. */
	fun getDrawableAt(location: Point2D): T? = getDrawableAt(location.x, location.y)

    fun getDrawables(): ImmutableList<T>

    /** Returns the [Drawable]s that match the specified predicate.*/
    fun getDrawables(predicate: (T) -> Boolean): ImmutableList<T>

    /** Returns the first [Drawable] that matches the specified predicate.*/
    fun getDrawable(predicate: (T) -> Boolean): T?

    /** Notifies this [DrawableContainer] that one of its child [Drawable]s has invalidated a certain region. */
    fun handleDrawableInvalidated(drawable: Drawable, region: RectangularShape)

    /** Notifies this [DrawableContainer] that one of its child [Drawable]s requests redrawing its invalidated region.*/
    fun handleDrawableRequestRedraw(drawable: Drawable)

    /** Notifies this [DrawableContainer] that one of its child [Drawable]s has updated its geometry.*/
    fun handleDrawableUpdated(drawable: Drawable)

    /**
     * Returns the position of a [Drawable] in the stacking order of this [DrawableContainer],
     * where 0 represents the topmost position.
     * @throws NoSuchElementException if this [DrawableContainer] doesn't contain `drawable`
     */
    fun getStackingOrderPosition(drawable: Drawable): Int

    /**
     * Sets the stacking order position of the specified [Drawable], where 0 represents the topmost position.
     * @throws NoSuchElementException if `drawable`
     * @throws IndexOutOfBoundsException if `position` is not a valid stacking order position
     */
    fun setStackingOrderPosition(position: Int, drawable: Drawable)

    /**
     * Converts a [Collection] of [Drawable]s into a [List] of [StackingOrderPosition]
     * that is ascending sorted by stacking order positions, i.e. starting with the topmost [Drawable] at
     * stacking order position 0.
     */
    fun getStackingOrderPositions(drawables: Collection<Drawable>): List<StackingOrderPosition>

    /**
     * Brings the specified [Drawable]s to the front of the stacking order while maintaining their relative
     * stacking order positions.
     */
    fun toFront(drawables: Collection<T>)

    /**
     * Brings the specified [Drawable]s to the back of the stacking order while maintaining their relative
     * stacking order positions.
     */
    fun toBack(drawables: Collection<T>)
}

data class StackingOrderPosition(val position: Int, val drawable: Drawable) : Comparable<StackingOrderPosition> {
    override fun compareTo(other: StackingOrderPosition): Int {
        return this.position.compareTo(other.position)
    }
}