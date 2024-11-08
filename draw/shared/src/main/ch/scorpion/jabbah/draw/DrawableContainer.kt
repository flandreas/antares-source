package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.style.Stylable
import ch.scorpion.jabbah.draw.style.StyleType

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
interface DrawableContainer<T : Drawable> : Drawable, DrawableBag<T>, Locatable {

	override fun contains(x: Double, y: Double): Boolean = super<DrawableBag>.contains(x, y)

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

    /** Notifies this [DrawableContainer] that one of its child [Drawable]s has invalidated a certain region. */
    fun handleDrawableInvalidated(drawable: Drawable, region: RectangularShape)

    /** Notifies this [DrawableContainer] that one of its child [Drawable]s requests redrawing its invalidated region.*/
    fun handleDrawableRequestRedraw(drawable: Drawable)

    /** Notifies this [DrawableContainer] that one of its child [Drawable]s has updated its geometry.*/
    fun handleDrawableUpdated(drawable: Drawable)

    /**
     * Draws the contents of this [DrawableContainer] without any additional things that is typically added
     * when a [View] draws its content. In particular, this method ensures that all [Stylable]s whose
     * [StyleType.isBackdrop] is set are drawn first.
     */
    fun drawStandalone(context: DrawContext)
}
