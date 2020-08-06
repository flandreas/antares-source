package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.module.DrawModule
import kotlin.js.JsName

interface CanvasFactory<T : InputEventContext> {
	@JsName("create")
	fun create(
		id: String,
		viewFactory: ViewFactory<T> = DrawModule.viewFactory as ViewFactory<T>
	): Canvas
}

/**
 * Represents a target-specific rectangular drawing area.
 */
interface Canvas {

    /** Contains the [View] that effectively displays the [Drawable]s.*/
    val view: View<*>

    /** Contains the dimension of the target canvas.*/
    val dimension: Dimension2D

    /** The background [Color] of this [Canvas].*/
    var backgroundColor: Color

    /** Returns the current location of the mouse pointer.*/
    val mouseLocation: Point2D

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

    fun dispatchEvent(e: InputEvent)
}
