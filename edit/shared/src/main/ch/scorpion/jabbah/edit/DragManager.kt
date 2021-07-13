package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.app.DrawingAppService

typealias DragManagerFactory = (Editor) -> DragManager
/**
 * [DragManager] manages dragging of [Component]s within a [Drawing] on behalf of an [Editor].
 *
 * Drag operations are typically initiated by the [SelectionTool] operating in the [Editor].
 * The x,y coordinates in the method represent the mouse location in model space.
 *
 * Node that the term "Drag" used here differs from the one in "Drag & Drop", which refers to
 * dragging objects between UI elements in a platform-specific way, while "dragging" by [DragManager]
 * refers to dragging [Component]s within the same [Drawing] in a lightweight, platform-independent way.
 */
interface DragManager {

	val dropComponent: Component?

	fun registerPlugin(plugin: DragManagerPlugin)

	/**
	 * Called when the user presses the mouse button at [x], [y] over [component].
	 */
	fun prepareDrag(component: Component, x: Double, y: Double)

	/**
	 * Called after the user has dragged the mouse with pressed button to [x], [y]
	 * after [prepareDrag] has previously been called.
	 * Moves all selected [Component]s while snapping the move offset using [SnapManager].
	 */
	fun mouseDragged(x: Double, y: Double)

	/**
	 * Called after the user has released the mouse at [x], [y] after [prepareDrag] has previously been called.
	 * Completes moving the selected [Component]s by using an undoable [DrawingAppService] method.
	 */
	fun mouseReleased(x: Double, y: Double)

	/** Determines whether [Component]s can be moved if the user pressed the key in [KeyEvent].*/
	fun isMoveKey(event: KeyEvent): Boolean

	/**
	 * Instantly moves the selected [Component]s according to the key in [KeyEvent].
	 * @throws IllegalArgumentException if [event] doesn't comply with [isMoveKey]
	 */
	fun moveByKeyEvent(event: KeyEvent)

	/**
	 * Sets and/or moves a [Component] to be dragged into the [Editor]'s [Drawing] from the
	 * outside (native) drag&drop system.
	 * Adds [component] to the animation container, if not already present, and updates its location according
	 * to the specified [Point2D]. Removes the previously set [Component] if `null` is specified as [component].
	 * Invokes all registered [DragManagerPlugin.handleDragged].
	 */
	fun setDropComponent(component: Component?, location: Point2D?)

	/**
	 * Called when dropping [component] initialized by previous calls of [setDropComponent] has been finished.
	 * Invokes all registered [DragManagerPlugin.handleDragFinished] and returns their additional [Command] (if any)
	 * for execution (not only registration!).
	 */
	fun finishDrop(component: Component): Collection<Command>
}

/**
 * Additional logic to be registered with [DragManager] and to be executed whenever [Component]s are dragged.
 * Note that [DragManagerPlugin]s are only used for single [Component] selections!
 * Example: Highlight another [Component] as "drag target" for the [Component] being dragged.
 */
interface DragManagerPlugin {

	/** Performs additional logic when the user drags [component].*/
	fun handleDragged(editor: Editor, component: Component)

	/**
	 * The returned [Command] will be executed, not only registered.
	 */
	fun handleDragFinished(editor: Editor, component: Component): Collection<Command>
}