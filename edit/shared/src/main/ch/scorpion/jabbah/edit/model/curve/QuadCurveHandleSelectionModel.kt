package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.select.AbstractHandleSelectionModel
import ch.scorpion.jabbah.edit.select.Handle
import ch.scorpion.jabbah.edit.select.RectangularHandle

/**
 * A [SelectionModel] consisting of [Handle]s to be used for selecting and shaping a [QuadCurveComponent].
 */

class QuadCurveHandleSelectionModel(c: QuadCurveComponent) : AbstractHandleSelectionModel<QuadCurveComponent>(c) {

	/** Handles input events on an individual [Handle].*/
	private val pointHandler = QuadCurveInputEventHandler()

	/** ---- [AbstractHandleSelectionModel] */

	override fun createInputEventHandler(): InputEventHandler<EditInputEventContext> = QuadCurveEventHandler()

	override fun calculateRequiredHandlesCount(): Int = 3

	override fun updateHandlesImpl() {
		if (requiredHandlesCount != calculateRequiredHandlesCount()) {
			createHandles()
		}
		for (i in 0..2) {
			getHandle(i).location = component.getPointAt(i)
		}
	}

	/** ---- [QuadCurveHandleSelectionModel] */

	private fun createHandles() {
		clearHandles()
		for (i in 0..2) {
			addHandle(RectangularHandle(pointHandler))
		}
		requiredHandlesCount = 3
	}

	private inner class QuadCurveEventHandler : EventHandler() {
		private var oldLocation = Point2D()

		override fun dragHandleBegin(view: View<*>) {
			val index = getIndexOf(focusHandle!!)
			oldLocation = Point2D(component.getPointAt(index))
		}

		override fun dragHandleEnd(editor: Editor) {
			val index = getIndexOf(focusHandle!!)
			val newLocation = component.getPointAt(index)
			editor.commandManager.register(
				MoveQuadCurvePointCommand(component, editor, index, oldLocation, newLocation))
		}
	}

	private inner class QuadCurveInputEventHandler : InputEventHandlerAdapter<EditInputEventContext>() {
		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			context.view.setCursor(Cursor.CROSSHAIR)
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			component.setPointAt(getIndexOf((this@QuadCurveHandleSelectionModel.inputEventHandler as QuadCurveEventHandler).focusHandle!!), Point2D(context.x, context.y))
			return this
		}
	}
}