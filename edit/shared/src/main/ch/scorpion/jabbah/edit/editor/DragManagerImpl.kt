package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.snap.MultiComponentSnappable

class DragManagerImpl(
	private val editor: Editor,
	private val drawingAppService: DrawingAppService = EditModule.drawingAppService,
	plugins: Collection<DragManagerPlugin> = emptySet()
) : DragManager {

	companion object {
		private val LOG by logger(DragManagerImpl::class)
	}

	private val plugins = mutableSetOf<DragManagerPlugin>()

	/** The [Component] acting as reference for moving potentially many [Component]s. */
	private var movedReferenceComponent: Component? = null

	/** The location of [movedReferenceComponent] before moving it. */
	private var moveStartLocation = Point2D.ZERO

	/** Stores the location of [movedReferenceComponent] before the last drag operation.*/
	private var moveLastLocation = Point2D.ZERO

	/** Support for snapping multiple [Component]s while being moved. Initialized when starting to drag.*/
	private var multiComponentSnappable: MultiComponentSnappable? = null

	init {
		plugins.forEach { registerPlugin(it) }
	}

	/** ---- [DragManager] interface */

	/**
	 * The method [setDropComponent] gets called hundreds of times each second even if the
	 * mouse is not moved, especially on the JS platform. In order to limit the repainting load in the target [View],
	 * keep the snapped [Component] location and update the [Component]'s location only it has changed.
	 */
	private var snappedDropLocation: Point2D? = null

	/** The [Component] currently being dragged for dropping into the [Drawing].*/
	override var dropComponent: Component? = null

	override fun registerPlugin(plugin: DragManagerPlugin) {
		plugins.add(plugin)
	}

	override fun prepareDrag(component: Component, x: Double, y: Double) {
		movedReferenceComponent = component
		moveStartLocation = Point2D(movedReferenceComponent!!.location)
		moveLastLocation = Point2D(x, y)
	}

	override fun mouseDragged(x: Double, y: Double) {
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
		Movable.dragBy(selection, Point2D(dx + offset.x, dy + offset.y))

		if (selection.size == 1) {
			plugins.forEach { it.handleDragged(editor, selection.first()) }
		}

		moveLastLocation = Point2D(x + offset.x, y + offset.y)
		editor.drawing.validate()
	}

	override fun mouseReleased(x: Double, y: Double) {
		if (movedReferenceComponent != null) {
			val selection = editor.view.selectionManager.selection
			if (moveStartLocation != movedReferenceComponent?.location) {

				val additionalCommands = mutableListOf<Command>()
				if (selection.size == 1) {
					plugins.forEach { additionalCommands.addAll(it.handleDragFinished(editor, selection.first())) }
				}

				try {
					logMove("mouse")
					drawingAppService.move(
						selection,
						movedReferenceComponent!!.location.subtract(moveStartLocation),
						editor,
						register = true,
						additionalCommands = additionalCommands
					)
				} catch (e: Throwable) {
					LOG.error("mouseReleased: error '${e.message}'")
					editor.commandManager.rollbackTransaction()
				}
			}
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

	override fun setDropComponent(component: Component?, location: Point2D?) {
		if (component != null) {
			if (dropComponent != null) {
				val snap = editor.snapManager.snap(location!!.x, location.y)
				if (snap != snappedDropLocation) {
					snappedDropLocation = snap
					dropComponent!!.location = location.add(snap)
					plugins.forEach { it.handleDragged(editor, dropComponent!!) }
				}
			} else {
				dropComponent = component
				editor.view.animationContainer.add(component)
			}
			dropComponent!!.validate()
		} else {
			if (dropComponent != null) {
				editor.view.animationContainer.remove(dropComponent!!)
				editor.drawing.validate()
				dropComponent = null
			}
		}
	}

	override fun finishDrop(component: Component): Collection<Command> {
		val additionalCommands = mutableListOf<Command>()
		plugins.forEach { additionalCommands.addAll(it.handleDragFinished(editor, component)) }
		return additionalCommands
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