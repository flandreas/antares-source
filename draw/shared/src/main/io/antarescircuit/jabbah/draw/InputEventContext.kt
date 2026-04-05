package io.antarescircuit.jabbah.draw

import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.Cursor

/**
 * An argument object for [InputEventHandler]s.
 *
 * @property view the [View] in which the object that owns this [InputEventHandler] is displayed. Used for
 * accessing [View] properties, such as changing the [Cursor] during input handling.
 * @property x the origin event's x-coordinate (in view coordinated) transferred to model coordinate space
 * @property y the origin event's y-coordinate (in view coordinated) transferred to model coordinate space
 */
open class InputEventContext(
	val view: View<*>,
	val mouseEvent: MouseEvent? = null,
	val keyEvent: KeyEvent? = null,
	val x: Double = 0.0,
	val y: Double = 0.0,
	val readonly: Boolean = false
) {
	val location: Point2D = Point2D(x, y)

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

	override fun toString(): String = mouseEvent?.toString() ?: keyEvent?.toString() ?: ""
}