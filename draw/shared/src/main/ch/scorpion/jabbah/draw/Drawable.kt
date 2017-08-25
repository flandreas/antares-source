package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape

/**
 * A [Drawable] is an object with a graphical representation.
 */
interface Drawable {

    /** The parent [DrawableContainer] that contains this [Drawable].*/
    val parent: DrawableContainer<*>?

    /** The rectangular area that completely contains this [Drawable].*/
    val boundingBox: RectangularShape

    /** Determines if this [Drawable] is visible, i.e. whether is painted or not.*/
    var visible: Boolean

    /** Determines whether this [Drawable] can be mirrored horizontally and vertically.*/
    val canMirror: Boolean

    /** Accepts a [HierarchyVisitor] to visit this [Drawable] and possible hierarchy children.*/
    fun accept(visitor: HierarchyVisitor): Boolean

    /**
     * Returns the [InputEventHandler] that handles input events for this [Drawable].
     * If a [Drawable] doesn't want to handle input events at all, it can return an empty implementation that
     * could be statically provided by an abstract skeleton implementation.
     * TODO Must this really be generic?
     */
    fun <T: InputEventContext> getInputEventHandler(context: T): InputEventHandler<T>

    /** Adds a [DrawableListener] to listen for [DrawableEvent]s from this [Drawable].*/
    fun addDrawableListener(listener: DrawableListener)

    /** Removes the specified [DrawableListener] to stop listening for [DrawableEvent]s from this [Drawable].*/
    fun removeDrawableListener(listener: DrawableListener)

    /** Draws this [Drawable] in the specified [DrawContext]. */
    fun draw(context: DrawContext)

    /**
     * Notifies the parent and all registered listeners that the entire bounds of this [Drawable]
     * has become invalid and needs repainting.
     */
    fun invalidate()

    /**
     * Notifies the parent and all registered listeners that a certain region of this [Drawable]
     * has become invalid and needs repainting.
     */
    fun invalidate(region: RectangularShape)

    /** Validates this [Drawable] by requesting that it is repainted.*/
    fun validate()

    /** Determines whether the location with the specified coordinates is located within this [Drawable]. */
    fun contains(x: Double, y: Double): Boolean

    /** Determines whether the specified [Point2D] is located within this [Drawable]. */
    fun contains(p: Point2D): Boolean = contains(p.x, p.y)

    /**
     * Notifies this [Drawable] that is has been added to a [DrawableContainer].
     * As a reaction, this [Drawable] should store a reference to that parent [DrawableContainer] in order
     * to be able to call the parent's invalidate and repaint methods.
     */
    fun handleAdded(container: DrawableContainer<*>)

    /** Notifies this [Drawable] that it has been removed from its parent [DrawableContainer].*/
    fun <T: Drawable> handleRemoved(container: DrawableContainer<T>)

    /**
     * Mirrors the geometry of this [Drawable] at the vertical axis defined by the specified x-coordinate.
     * @throws UnsupportedOperationException if [canMirror] is `false`
     */
    fun mirrorHorizontally(x: Double)

    /**
     * Mirrors the geometry of this [Drawable] at the horizontal axis defined by the specified y-coordinate.
     * @throws UnsupportedOperationException if [canMirror] is `false`
     */
    fun mirrorVertically(y: Double)

    /**
     * Returns a short description of this [Drawable] to be displayed as a tool tip.
     *
     * @param x x-coordinate of the mouse position
     * @param y y-coordinate of the mouse position
     * @param width the width in pixels to which multi-line text is to be formatted, or `null` if no width is to be enforced
     * @return the tool tip text of this [Drawable]], or `null`if this [Drawable] doesn't want to display a
     *      text at the specified location.
     */
    fun getToolTipText(x: Double, y: Double, width: Int?): String?
}