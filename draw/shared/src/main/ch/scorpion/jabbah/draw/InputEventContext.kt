package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor

/**
 * An argument object for [InputEventHandler]s.
 *
 * @property view the [View] in which the object that owns this [InputEventHandler] is displayed. Used for
 * accessing [View] properties, such as changing the [Cursor] during input handling.
 */
open class InputEventContext(
    val view: View<*>,
    val mouseEvent: MouseEvent? = null,
    val keyEvent: KeyEvent? = null,
    val x: Double = 0.0,
    val y: Double = 0.0
) {
    val location: Point2D get() = Point2D(x, y)

    fun withXY(p: Point2D): InputEventContext = withXY(p.x, p.y)

    /** Returns a copy of this [InputEventContext] with other x and y coordinates*/
    open fun withXY(x: Double, y: Double): InputEventContext {
        return InputEventContext(
                view = this.view,
                mouseEvent = this.mouseEvent,
                keyEvent = this.keyEvent,
                x = x,
                y = y
        )
    }
}