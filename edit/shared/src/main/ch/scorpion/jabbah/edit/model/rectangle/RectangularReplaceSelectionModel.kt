package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.edit.style.EditTheme

/**
 * A [SelectionModel] for [AbstractRectangularComponent] to be used with [SelectionDrawingStrategy.REPLACE].
 */
class RectangularReplaceSelectionModel(
	component: AbstractRectangularComponent
) : AbstractSelectionModel<AbstractRectangularComponent>(component) {

	companion object {
		private val LOG by logger(RectangularReplaceSelectionModel::class)
	}

	private val eventHandler = EventHandler()

	private val handleSelectionModel = RectangularHandleSelectionModel(component)

	private var handlesDisplayed = false

	/** ---- [Drawable] */

	override val boundingBox: RectangularShape
		get() = component.boundingBox.expandBy(component.stroke.width.toDouble())

	override fun draw(context: DrawContext) {
		context.g.color = Themes.get<EditTheme>().selection.foregroundColor
		context.g.draw(component.shape)
	}

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		return eventHandler as InputEventHandler<T>
	}

	override fun contains(x: Double, y: Double): Boolean = handleSelectionModel.contains(x, y)

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

	/** ---- [RectangularReplaceSelectionModel] */

	private fun displayHandles(view: DrawingView<*>) {
		if (!handlesDisplayed) {
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

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			if (handlesDisplayed && handleSelectionModel.contains(context.x, context.y)) {
				val handler = handleSelectionModel.getInputEventHandler(context).mouseMoved(context)
				if (handler == null) {
					context.view.setCursor(Cursor.HAND)
				}
				return this
			}
			if (component.contains(context.x, context.y)) {
				displayHandles(context.drawingView())
				context.view.setCursor(Cursor.HAND)
				return this
			}
			hideHandles(context.drawingView())
			return null
		}

		override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			val mousePressed = handleSelectionModel.getInputEventHandler(context).mousePressed(context)
			if (mousePressed != null) return this else  return null;
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