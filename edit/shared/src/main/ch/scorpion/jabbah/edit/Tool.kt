package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.KeyListener
import ch.scorpion.jabbah.base.event.MouseEvent

/**
 * A [Tool] is a part of an [Editor] and provides means for interactively editing the [Component]s of a
 * [Drawing].
 */
interface Tool : KeyListener {

	/**
	 * Determines whether this [Tool] stays enabled (and thus receive events) even when the [Editor]'s [DrawingView] is not editable.
	 * This is typically not the case, but might be useful for tools like [SelectionTool], which is needed to
	 * inspect objects even when the [DrawingView] can't allow the user to change the displayed document.
	 */
	val enabledInUneditableView: Boolean get() = false

    /**
     * This method is called to initialize the [Tool] when it is set as an [Editor]'s current [Tool].
     * A typical action on activation is to set the mouse pointer to give the user an indication of what this [Tool]
     * does.
     */
    fun activate()

    /**
     * This method is called when this [Tool] has finished its work and a different [Tool] is set as the
     * [Editor]'s current [Tool].
     */
    fun deactivate()

    /**
     * This method is called when a user clicks a mouse button while this [Tool] is active.
     * @param e the original [MouseEvent]
     * @param x the x coordinate in model space where the mouse was pressed
     * @param y the y coordinate in model space where the mouse was pressed
     */
    fun mouseClicked(e: MouseEvent, x: Double, y: Double)

    /**
     * This method is called when a user presses a mouse button while this [Tool] is active.
     * @param e the original [MouseEvent]
     * @param x the x coordinate in model space where the mouse was pressed
     * @param y the y coordinate in model space where the mouse was pressed
     */
    fun mousePressed(e: MouseEvent, x: Double, y: Double)

    /**
     * This method is called when a user releases a mouse button while this [Tool] is active.
     * @param e the original [MouseEvent]
     * @param x the x coordinate in model space where the mouse was released
     * @param y the y coordinate in model space where the mouse was released
     */
    fun mouseReleased(e: MouseEvent, x: Double, y: Double)

    /**
     * This method is called when a user moves the mouse while this [Tool] is active.

     * @param e the original [MouseEvent]
     * @param x the x coordinate in model space where the mouse was moved
     * @param y the y coordinate in model space where the mouse was moved
     */
    fun mouseMoved(e: MouseEvent, x: Double, y: Double)

    /**
     * This method is called when a user drags the mouse while this [Tool] is active.
     * @param e the original [MouseEvent]
     * @param x the x coordinate in model space where the mouse was dragged
     * @param y the y coordinate in model space where the mouse was dragged
     */
    fun mouseDragged(e: MouseEvent, x: Double, y: Double)
}