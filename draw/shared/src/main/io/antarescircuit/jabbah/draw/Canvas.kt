package io.antarescircuit.jabbah.draw

import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.draw.graphics.Graphics2D
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D

/**
 * Represents a target-specific rectangular drawing area.
 */
interface Canvas : PropertyOwner<Any> {

	companion object {

		/** The name of the [dimension] property in [PropertyChangeEvent]s. */
		const val PROP_DIMENSION = "PROP_DIMENSION"

        /** The name of the [devicePixelRatio] property in [PropertyChangeEvent]s. */
        const val PROP_DEVICE_PIXEL_RATIO = "PROP_DEVICE_PIXEL_RATIO"
	}

	/**
     * The number of actual physical pixels used per UI coordinate system unit.
     * On hDPI systems, this is 2 (or even more). Only relevant on the JS platform when run in browsers.
     * On the JVM platform, this value is always 1.
     */
	val devicePixelRatio: Double

    /**
     * Contains the [View] that effectively displays the [Drawable]s.
     * Instances of [Canvas] typically set [View.canvas] when being constructed.
     */
    val view: View<*>

    /**
     * The dimension of the drawing area in the target canvas, which is equivalent to the dimension
     * of the image that is backing up the contents of the canvas. This also defines the coordinate system
     * used by [Graphics2D] drawing on this [Canvas].
     *
     * Note that this is NOT the same as the dimension of the target system's canvas UI element.
     * If [devicePixelRatio] is 2, this property's width and height are twice as large as the corresponding
     * sizes of the UI element.
     */
    val dimension: Dimension2D

    /** The background [Color] of this [Canvas].*/
    var backgroundColor: Color

    /** Returns the current location of the mouse pointer in view space.*/
    val mouseLocation: Point2D

	/**
	 * Determines whether this [Canvas] implementation is initially laid out and provides a stable
	 * [dimension] property right from the start, allowing [View] to apply its default zoom strategy
	 * without flickering.
	 */
	val initialLayout: Boolean

    /**
     * Returns `true` if this [Canvas] (i.e. its platform-specific implementation) currently has
     * the event focus.
     */
    val hasFocus: Boolean

    /** Request the event focus from the window system for this [View] in order to receive key events.*/
    fun requestViewFocus()

    /** Sets the [Cursor] of this [Canvas].*/
    fun setCursor(cursor: Cursor)

    /** Repaints the entire drawing area.*/
    fun repaint()

    /** Repaints the area specified by the location and dimension values.*/
    fun repaint(x: Int, y: Int, width: Int, height: Int)

    fun addMouseListener(l: MouseListener)

    fun removeMouseListener(l: MouseListener)

    fun addMouseMotionListener(l: MouseMotionListener)

    fun removeMouseMotionListener(l: MouseMotionListener)

    fun addMouseWheelListener(l: MouseWheelListener)

    fun removeMouseWheelListener(l: MouseWheelListener)

    fun addKeyListener(l: KeyListener)

    fun removeKeyListener(l: KeyListener)

    fun setToolTipText(text: String?)

	/**
	 * Dispatches the inner [InputEvent.event] (if set) to the platform-specific canvas, or else
	 * dispatches [e] to all registered listeners.
	 */
    fun dispatchEvent(e: InputEvent)
}

/** Posted by [Canvas] on its [EventBus] before a popup menu becomes visible.*/
data class PopupMenuEvent(val canvas: Canvas)
