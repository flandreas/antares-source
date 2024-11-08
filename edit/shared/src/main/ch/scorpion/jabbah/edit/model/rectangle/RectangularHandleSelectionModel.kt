package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.Status
import ch.scorpion.jabbah.base.StatusType
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.math.SIGMA
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.select.AbstractHandleSelectionModel
import ch.scorpion.jabbah.edit.select.Handle
import ch.scorpion.jabbah.edit.select.RectangularHandle
import kotlin.math.max
import kotlin.math.min

/**
 * A [SelectionModel] consisting of [Handle]s to be used for selecting and manipulating a [RectangularComponent].
 * If the [AbstractRectangularComponent]'s aspect ratio is to be maintained, [RectangularHandleSelectionModel]
 * only shows the 4 corner handles.
 */
class RectangularHandleSelectionModel(
	component: AbstractRectangularComponent
) : AbstractHandleSelectionModel<AbstractRectangularComponent>(component) {

	/** ---- State  */

	private val northWestHandle: Handle
	private val northEastHandle: Handle
	private val southEastHandle: Handle
	private val southWestHandle: Handle
	private val northHandle: Handle?
	private val eastHandle: Handle?
	private val southHandle: Handle?
	private val westHandle: Handle?

	/** ---- Life cycle  */

	init {
		northWestHandle = addHandle(RectangularHandle(NorthWestHandler()))
		northEastHandle = addHandle(RectangularHandle(NorthEastHandler()))
		southEastHandle = addHandle(RectangularHandle(SouthEastHandler()))
		southWestHandle = addHandle(RectangularHandle(SouthWestHandler()))

		northHandle = if (!component.maintainAspectRation) {
			addHandle(RectangularHandle(NorthHandler()))
		} else null
		eastHandle = if (!component.maintainAspectRation) {
			addHandle(RectangularHandle(EastHandler()))
		} else null
		southHandle = if (!component.maintainAspectRation) {
			addHandle(RectangularHandle(SouthHandler()))
		} else null
		westHandle = if (!component.maintainAspectRation) {
			addHandle(RectangularHandle(WestHandler()))
		} else null
	}

	/** ---- [AbstractHandleSelectionModel]  */

	override fun calculateRequiredHandlesCount(): Int {
		return 8
	}

	override fun updateHandlesImpl() {
		val r = component.shape
		northWestHandle.setLocation(r.x, r.y)
		northEastHandle.setLocation(r.x + r.width, r.y)
		southWestHandle.setLocation(r.x, r.y + r.height)
		southEastHandle.setLocation(r.x + r.width, r.y + r.height)

		northHandle?.setLocation(r.x + r.width / 2, r.y)
		eastHandle?.setLocation(r.x + r.width, r.y + r.height / 2)
		southHandle?.setLocation(r.x + r.width / 2, r.y + r.height)
		westHandle?.setLocation(r.x, r.y + r.height / 2)
	}

	override fun createInputEventHandler(): InputEventHandler<EditInputEventContext> {
		return RectangleEventHandler()
	}

	/** ---- [RectangularHandleSelectionModel]  */

	private fun reportSize() {
		Status.set(StatusType.Small, "w=${component.width.toInt()}, h=${component.height.toInt()}")
	}

	private var aspectRatio: Double = 0.0

	/**
	 * Handles input events by dispatching them to the appropriate [Handle] and manages overall changes of the
	 * selected [RectangularComponent] by creating an [ResizeRectangleCommand] after the mouse has been
	 * released.
	 */
	internal inner class RectangleEventHandler : EventHandler() {

		// ---- State

		/** Stores the old bound of the [RectangularComponent]. Used for creating an undoable [Command].  */
		private var oldBounds: Rectangle2D? = null

		// ---- EventHandler

		override fun dragHandleBegin(view: View<*>) {
			oldBounds = Rectangle2D(component.shape)
			aspectRatio = oldBounds?.let {
				if (it.height < SIGMA) {
					Double.MAX_VALUE
				} else {
					it.width / it.height
				}
			} ?: 0.0
		}

		override fun dragHandleEnd(context: EditInputEventContext) {
			if (oldBounds!! != component.shape) {
				context.editor.commandManager.register(
					ResizeRectangleCommand(context.editor, component.id, Rectangle2D(oldBounds!!), Rectangle2D(component.shape)))
			}
		}
	}

	internal inner class NorthWestHandler : InputEventHandlerAdapter<EditInputEventContext>() {

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			context.view.setCursor(Cursor.NW_RESIZE)
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			val bounds = component.shape
			val x = min(context.x, bounds.maxX)
			val y = min(context.y, bounds.maxY)
			if (component.maintainAspectRation) {
				val prefWidth = bounds.maxX - x
				val height = prefWidth / aspectRatio
				component.setFrame(bounds.maxX - prefWidth, bounds.maxY - height, prefWidth, height)
			} else {
				component.setFrame(x, y, bounds.width + (bounds.x - x), bounds.height + (bounds.y - y))
			}
			reportSize()
			return this
		}
	}

	internal inner class NorthHandler : InputEventHandlerAdapter<EditInputEventContext>() {

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			context.view.setCursor(Cursor.N_RESIZE)
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			val bounds = component.shape
			val y = min(context.y, bounds.maxY)
			component.setFrame(bounds.x, y, bounds.width, bounds.height + (bounds.y - y))
			reportSize()
			return this
		}
	}

	internal inner class NorthEastHandler : InputEventHandlerAdapter<EditInputEventContext>() {

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			context.view.setCursor(Cursor.NE_RESIZE)
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			val bounds = component.shape
			val x = max(context.x, bounds.x)
			val y = min(context.y, bounds.maxY)
			if (component.maintainAspectRation) {
				val prefWidth = x - bounds.x
				val height = prefWidth / aspectRatio
				component.setFrame(bounds.minX, bounds.maxY - height, prefWidth, height)
			} else {
				component.setFrame(bounds.x, y, x - bounds.x, bounds.height + (bounds.y - y))
			}
			reportSize()
			return this
		}
	}

	internal inner class EastHandler : InputEventHandlerAdapter<EditInputEventContext>() {

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			context.view.setCursor(Cursor.E_RESIZE)
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			val bounds = component.shape
			val x = max(context.x, bounds.x)
			component.setFrame(bounds.x, bounds.y, x - bounds.x, bounds.height)
			reportSize()
			return this
		}
	}

	internal inner class SouthEastHandler : InputEventHandlerAdapter<EditInputEventContext>() {

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			context.view.setCursor(Cursor.SE_RESIZE)
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			val bounds = component.shape
			val x = max(context.x, bounds.x)
			val y = max(context.y, bounds.y)
			if (component.maintainAspectRation) {
				val prefHeight = y - bounds.y
				component.setFrame(bounds.x, bounds.y, aspectRatio * prefHeight, prefHeight)
			} else {
				component.setFrame(bounds.x, bounds.y, x - bounds.x, y - bounds.y)
			}
			reportSize()
			return this
		}
	}

	internal inner class SouthHandler : InputEventHandlerAdapter<EditInputEventContext>() {

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			context.view.setCursor(Cursor.S_RESIZE)
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			val bounds = component.shape
			val y = max(context.y, bounds.y)
			component.setFrame(bounds.x, bounds.y, bounds.width, y - bounds.y)
			reportSize()
			return this
		}
	}

	internal inner class SouthWestHandler : InputEventHandlerAdapter<EditInputEventContext>() {

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			context.view.setCursor(Cursor.SW_RESIZE)
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			val bounds = component.shape
			val x = min(context.x, bounds.maxX)
			val y = max(context.y, bounds.y)
			if (component.maintainAspectRation) {
				val prefHeight = y - bounds.y
				val width = aspectRatio * prefHeight
				component.setFrame(bounds.maxX - width, bounds.y, width, prefHeight)
			} else {
				component.setFrame(x, bounds.y, bounds.maxX - x, y - bounds.y)
			}
			reportSize()
			return this
		}
	}

	internal inner class WestHandler : InputEventHandlerAdapter<EditInputEventContext>() {

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			context.view.setCursor(Cursor.W_RESIZE)
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			val bounds = component.shape
			val x = min(context.x, bounds.maxX)
			component.setFrame(x, bounds.y, bounds.width + (bounds.x - x), bounds.height)
			reportSize()
			return this
		}
	}
}