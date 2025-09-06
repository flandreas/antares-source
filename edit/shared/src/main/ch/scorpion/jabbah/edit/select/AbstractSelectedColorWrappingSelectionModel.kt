package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.SelectionModel

/**
 * A [SelectedColorSelectionModel] that wraps another [SelectionModel] by drawing a [Component] simply in selection
 * color when being selected, but forwarding input events to an "inner" [SelectionModel], typically one that
 * shows [Handle]s when the mouse moves over the [SelectedColorSelectionModel]. This is used to display [Handle]s
 * only when really needed, and not when the [Component] is just to be shown as being selected.
 *
 * @param T the type of [Component] being selected by this [SelectionModel]
 */
abstract class AbstractSelectedColorWrappingSelectionModel<T : Component>(component: T) : SelectedColorSelectionModel<T>(component) {

	protected val handleSelectionModel: AbstractHandleSelectionModel<T> = createInnerSelectionModel(component)

	private var handlesDisplayed = false

	private val eventHandler = EventHandler()

	/** ---- [Drawable] */

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		@Suppress("UNCHECKED_CAST")
		return eventHandler as InputEventHandler<T>
	}

	/** ---- [AbstractSelectionModel] */

	override fun componentUpdated() {
		handleSelectionModel.componentUpdated()
		invalidate()
		validate()
	}

	override fun notifyAdded(view: DrawingView<*>) {
		if (contains(view.viewToModel(view.canvas.mouseLocation))) {
			displayHandles(view)
		}
	}

	override fun notifyRemoved(view: DrawingView<*>) {
		view.ghostContainer.remove(handleSelectionModel)
	}

	/** ---- [AbstractSelectedColorWrappingSelectionModel] */

	protected abstract fun createInnerSelectionModel(component: T): AbstractHandleSelectionModel<T>

	private fun displayHandles(view: DrawingView<*>) {
		if (view.editable && !handlesDisplayed) {
			view.ghostContainer.add(handleSelectionModel)
			handlesDisplayed = true
		}
	}

	private fun hideHandles(view: DrawingView<*>) {
		if (handlesDisplayed) {
			view.ghostContainer.remove(handleSelectionModel)
			view.ghostContainer.validate()
			handlesDisplayed = false
		}
	}

	private inner class EventHandler : InputEventHandlerAdapter<EditInputEventContext>() {

		override fun keyPressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			return handleSelectionModel.getInputEventHandler(context).keyPressed(context)
		}

		override fun keyReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			return handleSelectionModel.getInputEventHandler(context).keyReleased(context)
		}

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			if (handlesDisplayed && handleSelectionModel.contains(context.x, context.y)) {
				val handler = handleSelectionModel.getInputEventHandler(context).mouseMoved(context)
				if (handler == null) {
					context.view.setCursor(Cursor.MOVE)
				}
				return this
			}
			if (component.contains(context.x, context.y)) {
				displayHandles(context.drawingView)
				context.view.setCursor(Cursor.MOVE)
				return this
			}
			hideHandles(context.drawingView)
			return null
		}

		override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			val mousePressed = handleSelectionModel.getInputEventHandler(context).mousePressed(context)
			return if (mousePressed != null) this else null
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			handleSelectionModel.getInputEventHandler(context).mouseDragged(context)
			return this
		}

		override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			handleSelectionModel.getInputEventHandler(context).mouseReleased(context)
			return this
		}
	}
}