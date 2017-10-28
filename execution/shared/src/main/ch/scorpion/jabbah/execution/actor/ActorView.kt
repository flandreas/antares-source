package ch.scorpion.jabbah.execution.actor

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
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

interface ActorInteractionHandler {

    fun mouseMoved(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double)

    fun mousePressed(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double)

    fun mouseDragged(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double)

    fun mouseReleased(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double)

    fun mouseClicked(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double)

    fun keyPressed(signalHandler: SignalHandler, event: KeyEvent)
}

open class ActorInteractionHandlerAdapter : ActorInteractionHandler {

    override fun mouseMoved(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double) {
        // empty
    }

    override fun mousePressed(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double) {
        // empty
    }

    override fun mouseDragged(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double) {
        // empty
    }

    override fun mouseReleased(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double) {
        // empty
    }

    override fun mouseClicked(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double) {
        // empty
    }

    override fun keyPressed(signalHandler: SignalHandler, event: KeyEvent) {
        // empty
    }
}

@Suppress("unused")
object EmptyActorInteractionHandler : ActorInteractionHandlerAdapter()