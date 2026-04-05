package io.antarescircuit.jabbah.execution.actor

import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.execution.SignalHandler

/**
 * A graphical representation of an [Actor] that supports interaction with the [Actor] by the user.
 */
interface ActorView {

    /**
     * Returns the [InputEventHandler] that handles user interactions on this [ActorView] during execution,
     * or ´null` if this [ActorView] doesn't react to input events during execution.
     */
    fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler

    /**
     * Returns a short description of this [ActorView] to be displayed as a tool tip during simulation.
     * @param x the x-coordinate of the mouse position
     * @param y the y-coordinate of the mouse position
     * @return the tool tip text of this [ActorView].
     */
    fun <T: InputEventContext> getExecutionTooltip(context: T): Tooltip? = null

	fun executionStarted(signalHandler: SignalHandler) {}

	fun executionStartDone(signalHandler: SignalHandler) {}

	fun executionStopped(signalHandler: SignalHandler) {}
}

open class ActorInteractionContext(
	val signalHandler: SignalHandler,
	view: View<*>,
	mouseEvent: MouseEvent? = null,
	keyEvent: KeyEvent? = null,
	x: Double = 0.0,
	y: Double = 0.0
) : InputEventContext(view, mouseEvent, keyEvent, x, y) {

	/** Returns a copy of this [ActorInteractionContext] with other x and y coordinates*/
	override fun withXY(x: Double, y: Double): ActorInteractionContext {
		return ActorInteractionContext(
			signalHandler = signalHandler,
			view = view,
			mouseEvent = this.mouseEvent,
			keyEvent = this.keyEvent,
			x = x,
			y = y
		)
	}
}

typealias ActorInteractionHandler = InputEventHandler<ActorInteractionContext>

/** An [InputEventHandler] that displays [Cursor.CLICK] in [mouseMoved].*/
open class ClickableActorInteractionHandlerAdapter : InputEventHandlerAdapter<ActorInteractionContext>() {

	override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
		context.view.setCursor(Cursor.CLICK)
		return null
	}
}
