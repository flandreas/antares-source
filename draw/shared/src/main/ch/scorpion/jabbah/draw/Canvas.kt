package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Represents a target-specific rectangular drawing area.
 */
interface Canvas : PropertyOwner<Any> {

	companion object {

		/** The name of the [dimension] property in [PropertyChangeEvent]s. */
		const val PROP_DIMENSION = "PROP_DIMENSION"
	}

	/** The number of actual physical pixels used per view pixel. */
	val devicePixelRatio: Int

    /**
     * Contains the [View] that effectively displays the [Drawable]s.
     * Instances of [Canvas] typically set [View.canvas] when being constructed.
     */
    val view: View<*>

    /** Contains the dimension of the target canvas.*/
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
