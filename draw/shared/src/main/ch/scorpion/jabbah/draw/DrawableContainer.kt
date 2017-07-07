package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape

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
 * the appropriate handle()-methods of its parent [DrawableContainer] whenever needed.
 *
 * @param T the type of [Drawable]s that this [DrawableContainer] contains
 */
interface DrawableContainer<T : Drawable> : Drawable {

    /** Holds the number of [Drawable]s this [DrawableContainer] contains.*/
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
    fun contains(drawable: T): Boolean

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
    fun remove(drawable: T): DrawableContainer<T>

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

    /** Returns the first [Drawable] (in stacking order) that contains the specified location.*/
    fun getDrawableAt(x: Double, y: Double): T?

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
     * Returns the position of a [Drawable] in the stacking order of this [DrawableContainer].
     * @throws NoSuchElementException if this [DrawableContainer] doesn't contain `drawable`
     */
    fun getStackingOrderPosition(drawable: T): Int

    /**
     * Sets the stacking order position of the specified [Drawable]
     * @throws NoSuchElementException if `drawable`
     * @throws IndexOutOfBoundsException if `position` is not a valid stacking order position
     */
    fun setStackingOrderPosition(position: Int, drawable: T)

    /**
     * Converts a [Collection] of [Drawable]s into a [List] of [StackingOrderPosition]
     * that is ascending sorted by stacking order positions, i.e. starting with the topmost [Drawable] at
     * stacking order position 0.
     */
    fun getStackingOrderPositions(drawables: Collection<T>): List<StackingOrderPosition<T>>

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

data class StackingOrderPosition<T: Drawable>(val position: Int, val drawable: T) : Comparable<StackingOrderPosition<T>> {
    override fun compareTo(other: StackingOrderPosition<T>): Int {
        return this.position.compareTo(other.position)
    }
}