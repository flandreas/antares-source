package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.edit.*

/**
 * A base implementation of a [SelectionModel] that contains [Handle]s.
 */
abstract class AbstractHandleSelectionModel<T : Component>(
	component: T
) : AbstractSelectionModel<T>(component), UnzoomableSelectionModel<T> {

	/** Holds all [Handle]s of which this [SelectionModel] consists. */
	private val handles: MutableList<Handle> = mutableListOf()

	/** An [InputEventHandler] that forwards input events to the [Handle]s.*/
	protected val inputEventHandler by lazy { createInputEventHandler() }

	protected var requiredHandlesCount: Int = 0

	/** ---- [Unzoomable] */

	override var zoomPan: ZoomPan? = null
		set(value) {
			invalidate()
			field = value
			handles.forEach { it.zoomPan = field }
			updateHandles()
		}

	/** ---- [Drawable] */

	private val boundingBoxBuffer = Rectangle2D()

	override val boundingBox: Rectangle2D
		get() = Rectangle2D(boundingBoxBuffer)

	override fun <C : InputEventContext> getInputEventHandler(context: C): InputEventHandler<C> {
		@Suppress("UNCHECKED_CAST")
		return inputEventHandler as InputEventHandler<C>
	}

	override fun draw(context: DrawContext) {
		handles.forEach { it.draw(context) }
	}

	override fun contains(x: Double, y: Double): Boolean {
		return boundingBoxBuffer.contains(x, y)
	}

	/** ---- [AbstractSelectionModel] */

	override fun notifyAdded(view: DrawingView<*>) {
		componentUpdated()
		requiredHandlesCount = calculateRequiredHandlesCount()
	}

	/**
	 * Updates all handles according to the current zoom factor and the geometry of the current selected
	 * [Component].
	 */
	override fun componentUpdated() {
		updateHandles()
	}

	/** ---- [AbstractHandleSelectionModel] */

	/**
	 * Creates an [InputEventHandler] that is used by this [SelectionModel] to handle all incoming input
	 * events. Extending classes can use [EventHandler] as a base class for implementing appropriate event handlers.
	 */
	protected abstract fun createInputEventHandler(): InputEventHandler<EditInputEventContext>

	/**
	 * Calculates the number of required [Handle]s based on the current state of the [Component] that is
	 * selected by this [SelectionModel].
	 * The returned value is used check whether the number of needed [Handle]s has been changed, and the
	 * [Handle]s maintained by this [AbstractHandleSelectionModel] should be recreated.
	 */
	protected abstract fun calculateRequiredHandlesCount(): Int

	/**
	 * Updates all handles according to the current zoom factor and the geometry of the current selected
	 * [Component].
	 */
	protected abstract fun updateHandlesImpl()

	/**
	 * Adds a new [Handle] at the end of the list of already added [Handle]s.
	 * @param handle the [Handle] to be added.
	 * @return the [Handle] that has been added. Returned for easy call concatenation.
	 */
	protected fun addHandle(handle: Handle): Handle = addHandle(handles.size, handle)

	protected fun addHandle(index: Int, handle: Handle): Handle {
		handles.add(index, handle)
		if (zoomPan != null) {
			handle.zoomPan = zoomPan
		}
		return handle
	}

	protected fun getHandlesCount(): Int = handles.size

	protected fun getHandle(index: Int): Handle = handles[index]

	protected fun getIndexOf(handle: Handle): Int = handles.indexOf(handle)

	protected fun getHandleAt(x: Double, y: Double): Handle? = handles.firstOrNull { it.contains(x, y) }

	protected fun clearHandles() {
		handles.clear()
	}

	protected fun updateBoundingBox() {
		if (handles.isEmpty()) {
			boundingBoxBuffer.setFrame(0.0, 0.0, 0.0, 0.0)
			return
		}
		boundingBoxBuffer.setFrame(handles[0].boundingBox)
		for (i in 1 until handles.size) {
			boundingBoxBuffer.add(handles[i].boundingBox)
		}
	}

	protected fun updateHandles() {
		invalidate()
		updateHandlesImpl()
		updateBoundingBox()
		invalidate()
		validate()
	}

	/** Base implementation of an [InputEventHandler] that handles input events for a [SelectionModel] with [Handle]s.*/
	open inner class EventHandler : InputEventHandlerAdapter<EditInputEventContext>() {

		/**
		 * Holds the [Handle] that has currently the focus. Is set in [mousePressed] if the mouse is inside
		 * one of the [Handle]s. Set to `null`otherwise.
		 */
		var focusHandle: Handle? = null
			private set

		/**
		 * Gets called by this [EventHandler] in [.mousePressed]. This
		 * implementation is empty. Extending classes can overwrite this method in order to store the current state of
		 * the selected [Component], which is used again in [.dragHandleEnd] for creating undoable
		 * [Command]s.
		 *
		 * Before this method gets called, the focus handle is set to the [Handle] that is manipulated.
		 */
		protected open fun dragHandleBegin(view: View<*>) {
			// empty
		}

		/**
		 * Gets called by this [EventHandler] in [.mouseReleased]. This
		 * implementation is empty. Extending classes can overwrite this method in order to creating undoable
		 * [Command]s.
		 *
		 * When this method gets called, the focus handle is still set to the [Handle] that has been manipulated.
		 */
		protected open fun dragHandleEnd(editor: Editor) {
			// empty
		}

		/** ---- [InputEventHandlerAdapter] */

		override fun keyPressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			return if (focusHandle != null) this else null
		}

		override fun keyReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			return if (focusHandle != null) this else null
		}

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			val handle = getHandleAt(context.x, context.y) ?: return null
			handle.getInputEventHandler(context).mouseMoved(context)
			return this
		}

		override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			val handle = getHandleAt(context.x, context.y)
			if (handle == null) {
				focusHandle = null
				return null
			}
			focusHandle = handle
			dragHandleBegin(context.view)
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			if (focusHandle == null) {
				return null
			}
			val snap = context.editor.snapManager.snap(context.x, context.y)
			focusHandle?.getInputEventHandler(context)?.mouseDragged(
				context.withXY(context.x + snap.x, context.y + snap.y))
			return this
		}

		override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			if (focusHandle == null) {
				return null
			}
			dragHandleEnd(context.editor)
			focusHandle?.getInputEventHandler(context)?.mouseReleased(context)
			return this
		}
	}
}