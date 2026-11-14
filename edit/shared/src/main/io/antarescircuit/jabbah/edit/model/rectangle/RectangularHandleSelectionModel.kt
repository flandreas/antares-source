package io.antarescircuit.jabbah.edit.model.rectangle

import io.antarescircuit.jabbah.base.Status
import io.antarescircuit.jabbah.base.StatusType
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.math.SIGMA
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractHandleSelectionModel
import io.antarescircuit.jabbah.edit.select.Handle
import io.antarescircuit.jabbah.edit.select.RectangularHandle
import kotlin.math.max
import kotlin.math.min

/**
 * A [SelectionModel] consisting of [Handle]s to be used for selecting and manipulating a [RectangularComponent].
 * If the [ResizableRectangularComponent]'s aspect ratio is to be maintained, [RectangularHandleSelectionModel]
 * only shows the 4 corner handles.
 */
class RectangularHandleSelectionModel<T : ResizableRectangularComponent>(
	component: T
) : AbstractHandleSelectionModel<T>(component) {

	companion object {
		private val LOG by logger(RectangularHandleSelectionModel::class)
	}

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

		for (i in 0 until getHandlesCount()) {
			val handle = getHandle(i)
			handle.location = component.handleToWorld(handle.location)
		}
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
				LOG.userTrail("Resize '${component.type}' ${component.id} to ${component.shape}")
				context.editor.commandManager.register(
					ResizeRectangleCommand(
						context.editor,
						component.id,
						Rectangle2D(oldBounds!!),
						Rectangle2D(component.shape))
				)
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
			val local = component.worldToHandle(context.location)
			val x = min(local.x, bounds.maxX - component.minimumWidth)
			val y = min(local.y, bounds.maxY - component.minimumHeight)
			if (component.maintainAspectRation) {
				val prefWidth = bounds.maxX - x
				val height = prefWidth / aspectRatio
				component.resizeFrame(bounds.maxX - prefWidth, bounds.maxY - height, prefWidth, height)
			} else {
				component.resizeFrame(x, y, bounds.width + (bounds.x - x), bounds.height + (bounds.y - y))
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
			val local = component.worldToHandle(context.location)
			val y = min(local.y, bounds.maxY - component.minimumHeight)
			component.resizeFrame(bounds.x, y, bounds.width, bounds.height + (bounds.y - y))
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
			val local = component.worldToHandle(context.location)
			val x = max(local.x, bounds.x + component.minimumWidth)
			val y = min(local.y, bounds.maxY - component.minimumHeight)
			if (component.maintainAspectRation) {
				val prefWidth = x - bounds.x
				val height = prefWidth / aspectRatio
				component.resizeFrame(bounds.minX, bounds.maxY - height, prefWidth, height)
			} else {
				component.resizeFrame(bounds.x, y, x - bounds.x, bounds.height + (bounds.y - y))
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
			val local = component.worldToHandle(context.location)
			val x = max(local.x, bounds.x + component.minimumWidth)
			component.resizeFrame(bounds.x, bounds.y, x - bounds.x, bounds.height)
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
			val local = component.worldToHandle(context.location)
			val x = max(local.x, bounds.x + component.minimumWidth)
			val y = max(local.y, bounds.y + component.minimumHeight)
			if (component.maintainAspectRation) {
				val prefHeight = y - bounds.y
				component.resizeFrame(bounds.x, bounds.y, aspectRatio * prefHeight, prefHeight)
			} else {
				component.resizeFrame(bounds.x, bounds.y, x - bounds.x, y - bounds.y)
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
			val local = component.worldToHandle(context.location)
			val y = max(local.y, bounds.y + component.minimumHeight)
			component.resizeFrame(bounds.x, bounds.y, bounds.width, y - bounds.y)
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
			val local = component.worldToHandle(context.location)
			val x = min(local.x, bounds.maxX - component.minimumWidth)
			val y = max(local.y, bounds.y + component.minimumHeight)
			if (component.maintainAspectRation) {
				val prefHeight = y - bounds.y
				val width = aspectRatio * prefHeight
				component.resizeFrame(bounds.maxX - width, bounds.y, width, prefHeight)
			} else {
				component.resizeFrame(x, bounds.y, bounds.maxX - x, y - bounds.y)
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
			val local = component.worldToHandle(context.location)
			val x = min(local.x, bounds.maxX - component.minimumWidth)
			component.resizeFrame(x, bounds.y, bounds.width + (bounds.x - x), bounds.height)
			reportSize()
			return this
		}
	}
}