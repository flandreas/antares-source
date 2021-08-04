package ch.scorpion.jabbah.edit.drag

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

	/**
	 * Set to `true` if any of the registered plugins implements [DragManagerDestinationPlugin], in which
	 * case destination [Component]s as drag targets are located.
	 */
	private var pluginsNeedDestination: Boolean = false

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
		if (plugin is DragManagerPlugin) {
			pluginsNeedDestination = true
		}
	}

	override fun prepareDrag(component: Component, x: Double, y: Double) {
		movedReferenceComponent = component
		moveStartLocation = Point2D(movedReferenceComponent!!.location)
		moveLastLocation = Point2D(x, y)
	}

	override fun mouseDragged(x: Double, y: Double) {
		val selection = editor.view.selectionManager.selection

		dragSnapped(selection, x, y)

		if (selection.size == 1) {
			involvePluginsDragged(selection.first())
		}

		editor.drawing.validate()
	}

	private fun dragSnapped(components: Collection<Component>, x: Double, y: Double) {
		val dx = x - moveLastLocation.x
		val dy = y - moveLastLocation.y
		var snap = Point2D.ZERO

		if (editor.snapManager.snapEnabled) {
			if (components.size > 1) {
				if (multiComponentSnappable == null) {
					multiComponentSnappable = MultiComponentSnappable(components)
				}
				snap = editor.snapManager.snap(multiComponentSnappable!!, dx, dy)
			} else if (components.size == 1) {
				snap = editor.snapManager.snap(components.first(), dx, dy)
			}
		}

		val offset =  Point2D(dx + snap.x, dy + snap.y)

		// Move all selected [Components] by the same snapped offset
		Movable.dragBy(components, offset)
		moveLastLocation = Point2D(x + snap.x, y + snap.y)
	}

	private fun involvePluginsDragged(component: Component) {
		var destination: Component? = null
		if (pluginsNeedDestination) {
			destination = editor.drawing.getDrawableAt(component.boundingBox) { it !== component }
		}
		plugins.forEach {
			if (it is DragManagerDestinationPlugin) {
				it.handleDragged(editor, component, destination)
			} else {
				it.handleDragged(editor, component)
			}
		}
	}

	override fun mouseReleased(x: Double, y: Double) {
		if (movedReferenceComponent != null) {
			val selection = editor.view.selectionManager.selection
			if (moveStartLocation != movedReferenceComponent?.location) {

				val additionalCommands = if (selection.size == 1) {
					involvePluginsDragFinished(selection.first())
				} else {
					emptyList()
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
					throw e
				}
			}
		}

		involvePluginsDragTerminated()
	}

	private fun involvePluginsDragFinished(component: Component): List<Command> {
		val commands = mutableListOf<Command>()
		plugins.forEach { commands.addAll(it.handleDragFinished(editor, component)) }
		return commands
	}

	private fun involvePluginsDragTerminated() {
		multiComponentSnappable = null
		movedReferenceComponent = null
		dropComponent = null
		plugins.forEach { it.handleDragTerminated(editor) }
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
				dragSnapped(listOf(component), location!!.x, location.y)
				involvePluginsDragged(component)
			} else {
				component.location = location!!
				prepareDrag(component, location.x, location.y)
				dropComponent = component
				editor.view.animationContainer.add(component)
			}
			dropComponent!!.validate()
		} else {
			if (dropComponent != null) {
				editor.view.animationContainer.remove(dropComponent!!)
				editor.drawing.validate()
			}
			involvePluginsDragTerminated()
		}
	}

	override fun finishDrop(component: Component): Collection<Command> =
		involvePluginsDragFinished(component)

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