package ch.scorpion.jabbah.execution.actor

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.execution.SignalHandler

/**
 * A graphical representation of an [Actor] that supports interaction with the [Actor] by the user.
 */
interface ActorView {

    /**
     * Returns the [ActorInteractionHandler] that handles user interactions on this [ActorView],
     * or ´null` if this [ActorView] doesn't react to input events during execution.
     */
    fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler?

    /**
     * Returns a short description of this [ActorView] to be displayed as a tool tip during simulation.
     * @param x the x-coordinate of the mouse position
     * @param y the y-coordinate of the mouse position
     * @return the tool tip text of this [ActorView].
     */
    fun getExecutionTooltip(x: Double, y: Double): Tooltip?
}

interface ActorInteractionContext {

	val signalHandler: SignalHandler
	val view: View<*>
	val mouseEvent: MouseEvent?
	val keyEvent: KeyEvent?
	val x: Double
	val y: Double

	/** Returns a copy of this [ActorInteractionContext] with other x and y coordinates*/
	fun withXY(x: Double, y: Double): ActorInteractionContext
}

class ActorInteractionContextImpl(
	override val signalHandler: SignalHandler,
	override val view: View<*>,
	override val mouseEvent: MouseEvent? = null,
	override val keyEvent: KeyEvent? = null,
	override val x: Double = 0.0,
	override val y: Double = 0.0
) : ActorInteractionContext {

	/** Returns a copy of this [ActorInteractionContext] with other x and y coordinates*/
	override fun withXY(x: Double, y: Double): ActorInteractionContext {
		return ActorInteractionContextImpl(
			signalHandler = signalHandler,
			view = view,
			mouseEvent = this.mouseEvent,
			keyEvent = this.keyEvent,
			x = x,
			y = y
		)
	}
}

/**
 * A part of an [ActorView] that handles input events.
 *
 * All [ActorInteractionHandler] methods return the [ActorInteractionHandler] that should receive the next mouse input
 * (including this handler itself), or `null` if the next recipient cannot be determined.
 */
interface ActorInteractionHandler {

    fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler?

    fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler?

    fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler?

    fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler?

    fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler?

    fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler?
}

open class ActorInteractionHandlerAdapter : ActorInteractionHandler {

    override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
        return null
    }

    override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
	    return null
    }

    override fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler? {
	    return null
    }

    override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? {
	    return null
    }

    override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
	    return null
    }

    override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
	    return null
    }
}

/** An [ActorInteractionHandlerAdapter] that displays [Cursor.HAND] in [mouseMoved].*/
open class ClickableActorInteractionHandlerAdapter : ActorInteractionHandlerAdapter() {

	override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
		context.view.setCursor(Cursor.HAND)
		return null
	}
}

@Suppress("unused")
object EmptyActorInteractionHandler : ActorInteractionHandlerAdapter()