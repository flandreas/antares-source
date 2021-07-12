package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.edit.app.DrawingAppService

/**
 * [DragManager] manages dragging of [Component]s within a [Drawing] on behalf of an [Editor].
 *
 * Drag operations are typically initiated by the [SelectionTool] operating in the [Editor].
 * The x,y coordinates in the method represent the mouse location in model space
 */
interface DragManager {

	/**
	 * Called when the user presses the mouse button at [x], [y] over [component].
	 */
	fun prepareDrag(component: Component, x: Double, y: Double)

	/**
	 * Called after the user has dragged the mouse with pressed button to [x], [y]
	 * after [prepareDrag] has previously been called.
	 * Moves all selected [Component]s while snapping the move offset using [SnapManager].
	 */
	fun mouseDragged(e: MouseEvent, x: Double, y: Double)

	/**
	 * Called after the user has released the mouse at [x], [y] after [prepareDrag] has previously been called.
	 * Completes moving the selected [Component]s by using an undoable [DrawingAppService] method.
	 */
	fun mouseReleased(e: MouseEvent, x: Double, y: Double)

	/** Determines whether [Component]s can be moved if the user pressed the key in [KeyEvent].*/
	fun isMoveKey(event: KeyEvent): Boolean

	/**
	 * Instantly moves the selected [Component]s according to the key in [KeyEvent].
	 * @throws IllegalArgumentException if [event] doesn't comply with [isMoveKey]
	 */
	fun moveByKeyEvent(event: KeyEvent)
}