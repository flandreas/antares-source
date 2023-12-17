package ch.scorpion.jabbah.edit.drag

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.drawable.Movable
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.snap.MultiComponentSnappable
import kotlin.math.abs
import kotlin.math.max

class DragManagerImpl(
	private val editor: Editor,
	private val drawingAppService: DrawingAppService = EditModule.drawingAppService,
	plugins: Collection<DragManagerPlugin> = emptySet()
) : InputEventHandlerAdapter<InputEventContext>(), DragManager {

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

	/** The location of the mouse (model) at the start of dragging. */
	private var mouseStartLocation = Point2D.ZERO

	/** Stores the location of [movedReferenceComponent] before the last drag operation.*/
	private var moveLastLocation = Point2D.ZERO

	/** Support for snapping multiple [Component]s while being moved. Initialized when starting to drag.*/
	private var multiComponentSnappable: MultiComponentSnappable? = null

	/** The maximum drag distance reached during a single drag operation*/
	private var maxDragDistance: Int = 0

	init {
		plugins.forEach { registerPlugin(it) }
	}

	/** ---- [DragManager] interface */

	/** The [Component] currently being dragged for dropping into the [Drawing].*/
	override var dropComponent: Component? = null

	override fun registerPlugin(plugin: DragManagerPlugin) {
		plugins.add(plugin)
		pluginsNeedDestination = true
	}

	override fun prepareDrag(component: Component, x: Double, y: Double) {
		if (!component.isDragManager) {
			movedReferenceComponent = component
			moveStartLocation = Point2D(movedReferenceComponent!!.location)
			moveLastLocation = Point2D(x, y)
			mouseStartLocation = Point2D(x, y)
			maxDragDistance = 0
		}
	}

	override fun mouseDragged(context: InputEventContext): InputEventHandler<InputEventContext>? {
		val selection = editor.view.selectionManager.selection
		if (selection.size == 1 && selection.first().isDragManager) {
			return null
		}

		dragSnapped(selection, context.x, context.y, orthogonal = context.mouseEvent?.isShiftDown == true, doSnap = context.mouseEvent?.isAltDown != true)

		if (selection.size == 1) {
			involvePluginsDragged(selection.first())
		}

		editor.drawing.validate()
		return null
	}

	private fun dragSnapped(components: Collection<Component>, x: Double, y: Double, orthogonal: Boolean, doSnap: Boolean) {
		if (components.isEmpty() || movedReferenceComponent == null) {
			return
		}

		// Calculate the total mouse move vector (non-snapped), because switching from/to
		// orthogonal move must refer to the initial Component location
		var dx = x - mouseStartLocation.x
		var dy = y - mouseStartLocation.y

		if (orthogonal) {
			if (abs(dx) >= abs(dy)) {
				// move horizontally
				dy = 0.0
			} else {
				// move vertically
				dx = 0.0
			}
		}

		maxDragDistance = max(
			maxDragDistance,
			mouseStartLocation.distance(moveStartLocation.x + dx, moveStartLocation.y + dy).toInt()
		)

		// Calculate the delta move using the reference Component
		val delta = Point2D(moveStartLocation.x + dx, moveStartLocation.y + dy)
			.subtract(movedReferenceComponent!!.location)

		// Snap the new location
		var snap = Point2D.ZERO
		if (doSnap && editor.snapManager.snapEnabled) {
			if (components.size > 1) {
				if (multiComponentSnappable == null) {
					multiComponentSnappable = MultiComponentSnappable(components)
				}
				snap = editor.snapManager.snap(multiComponentSnappable!!, delta.x, delta.y)
			} else if (components.size == 1) {
				snap = editor.snapManager.snap(components.first(), delta.x, delta.y)
			}
		}

		// Move all selected [Components] by the same snapped offset
		Movable.moveBy(components, delta.x + snap.x, delta.y + snap.y)

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

	override fun mouseReleased(context: InputEventContext): InputEventHandler<InputEventContext>? {
		if (movedReferenceComponent != null) {
			val selection = editor.view.selectionManager.selection

			if (selection.size == 1 && selection.first().isDragManager) {
				return null
			}

			val additionalCommands = if (selection.size == 1 && maxDragDistance > 0) {
				involvePluginsDragFinished(selection.first())
			} else {
				emptyList()
			}

			if (additionalCommands.isNotEmpty() || moveStartLocation != movedReferenceComponent?.location) {
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
		return null
	}

	override fun keyPressed(context: InputEventContext): InputEventHandler<InputEventContext>? {
		if (movedReferenceComponent != null && context.keyEvent?.key == KeyEvent.VK_SHIFT) {
			val selection = editor.view.selectionManager.selection
			if (selection.size == 1 && selection.first().isDragManager) {
				return null
			}
			dragSnapped(selection, moveLastLocation.x, moveLastLocation.y, orthogonal = true, doSnap = true)
		}
		return null
	}

	override fun keyReleased(context: InputEventContext): InputEventHandler<InputEventContext>? {
		if (movedReferenceComponent != null &&  context.keyEvent?.key == KeyEvent.VK_SHIFT) {
			val selection = editor.view.selectionManager.selection
			if (selection.size == 1 && selection.first().isDragManager) {
				return null
			}
			dragSnapped(selection, moveLastLocation.x, moveLastLocation.y, orthogonal = false, doSnap = true)
		}
		return null
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
				dragSnapped(listOf(component), location!!.x, location.y, orthogonal = false, doSnap = true)
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
			LOG.userTrail("Move component '${selection.first().type}' with ID ${selection.first().id} by $action")
		} else {
			LOG.userTrail("Move ${selection.size} components by $action")
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