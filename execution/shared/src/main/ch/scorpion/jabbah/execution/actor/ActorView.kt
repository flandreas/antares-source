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
    fun getActorInteractionHandler(): ActorInteractionHandler?

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

interface ActorInteractionHandler {

    fun mouseMoved(context: ActorInteractionContext)

    fun mousePressed(context: ActorInteractionContext)

    fun mouseDragged(context: ActorInteractionContext)

    fun mouseReleased(context: ActorInteractionContext)

    fun mouseClicked(context: ActorInteractionContext)

    fun keyPressed(context: ActorInteractionContext)
}

open class ActorInteractionHandlerAdapter : ActorInteractionHandler {

    override fun mouseMoved(context: ActorInteractionContext) {
        // empty
    }

    override fun mousePressed(context: ActorInteractionContext) {
        // empty
    }

    override fun mouseDragged(context: ActorInteractionContext) {
        // empty
    }

    override fun mouseReleased(context: ActorInteractionContext) {
        // empty
    }

    override fun mouseClicked(context: ActorInteractionContext) {
        // empty
    }

    override fun keyPressed(context: ActorInteractionContext) {
        // empty
    }
}

/** An [ActorInteractionHandlerAdapter] that displays [Cursor.HAND] in [mouseMoved].*/
open class ClickableActorInteractionHandlerAdapter : ActorInteractionHandlerAdapter() {

	override fun mouseMoved(context: ActorInteractionContext) {
		context.view.setCursor(Cursor.HAND)
	}
}

@Suppress("unused")
object EmptyActorInteractionHandler : ActorInteractionHandlerAdapter()