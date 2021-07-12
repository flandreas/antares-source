package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DragManager
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Movable
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.snap.MultiComponentSnappable

class DragManagerImpl(
	private val editor: Editor,
	private val drawingAppService: DrawingAppService = EditModule.drawingAppService
) : DragManager {

	companion object {
		private val LOG by logger(DragManagerImpl::class)
	}

	/** The [Component] acting as reference for moving potentially many [Component]s. */
	private var movedReferenceComponent: Component? = null

	/** The location of [movedReferenceComponent] before moving it. */
	private var moveStartLocation = Point2D.ZERO

	/** Stores the location of [movedReferenceComponent] before the last drag operation.*/
	private var moveLastLocation = Point2D.ZERO

	/** Support for snapping multiple [Component]s while being moved. Initialized when starting to drag.*/
	private var multiComponentSnappable: MultiComponentSnappable? = null

	/** ---- [DragManager] interface */

	override fun prepareDrag(component: Component, x: Double, y: Double) {
		movedReferenceComponent = component
		moveStartLocation = Point2D(movedReferenceComponent!!.location)
		moveLastLocation = Point2D(x, y)
	}

	override fun mouseDragged(e: MouseEvent, x: Double, y: Double) {

		val dx = x - moveLastLocation.x
		val dy = y - moveLastLocation.y
		val selection = editor.view.selectionManager.selection
		var offset = Point2D.ZERO

		if (editor.snapManager.snapEnabled) {
			if (selection.size > 1) {
				if (multiComponentSnappable == null) {
					multiComponentSnappable = MultiComponentSnappable(selection)
				}
				offset = editor.snapManager.snap(multiComponentSnappable!!, dx, dy)
			} else if (selection.size == 1) {
				offset = editor.snapManager.snap(selection.first(), dx, dy)
			}
		}

		// Move all selected [Components] by the same snapped offset
		Movable.dragBy(editor, selection, Point2D(dx + offset.x, dy + offset.y))

		moveLastLocation = Point2D(x + offset.x, y + offset.y)
		editor.drawing.validate()
	}

	override fun mouseReleased(e: MouseEvent, x: Double, y: Double) {
		if (movedReferenceComponent != null) {
			val selection = editor.view.selectionManager.selection
			if (moveStartLocation != movedReferenceComponent?.location) {
				try {
					logMove("mouse")
					drawingAppService.move(
						selection,
						movedReferenceComponent!!.location.subtract(moveStartLocation),
						editor,
						register = true)
				} catch (e: Throwable) {
					LOG.error("mouseReleased: error '${e.message}'")
					editor.commandManager.rollbackTransaction()
				}
			}
			Movable.dragFinished(editor, selection)
		}

		multiComponentSnappable = null
		movedReferenceComponent = null
	}

	override fun isMoveKey(event: KeyEvent): Boolean =
		when(event.key) {
			KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT, KeyEvent.VK_UP, KeyEvent.VK_DOWN -> event.modifiers == 0
			else -> false
		}

	override fun moveByKeyEvent(event: KeyEvent) {
		logMove("key")
		drawingAppService.move(
			movables = editor.view.selectionManager.selection,
			offset = getKeyMoveDirection(event).toPoint2D().multiply(editor.view.grid.distance),
			editor,
			register = false
		)
	}

	/** ---- [DragManagerImpl] */

	private fun logMove(action: String) {
		val selection = editor.view.selectionManager.selection
		if (selection.size == 1) {
			LOG.debug("Move component '${selection.first().type}' with ID ${selection.first().id} by $action")
		} else {
			LOG.debug("Move ${selection.size} components by $action")
		}
	}

	private fun getKeyMoveDirection(event: KeyEvent): Direction {
		return when(event.key) {
			KeyEvent.VK_RIGHT -> Direction.EAST
			KeyEvent.VK_LEFT -> Direction.WEST
			KeyEvent.VK_UP -> Direction.NORTH
			KeyEvent.VK_DOWN -> Direction.SOUTH
			else -> throw IllegalArgumentException("KeyEvent doesn't represent a move direction")
		}
	}
}