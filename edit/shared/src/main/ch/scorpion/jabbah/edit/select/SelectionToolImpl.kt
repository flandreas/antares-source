package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.view.CurrentPanMethod
import ch.scorpion.jabbah.draw.view.TooltipHandler
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.tool.ToolAdapter

/**
 * Standard implementation of a [SelectionTool].
 * Uses a [RubberBandHandler] for selecting multiple [Component]s at a time.
 */
class SelectionToolImpl(
	editor: Editor,
	override val rubberBandHandler: RubberBandHandler,
	eventBus: EventBus
) : ToolAdapter(editor), SelectionTool {

	companion object {
		private val LOG by logger(SelectionToolImpl::class)
	}

	/** The target [InputEventHandler] to which events are forwarded during complex interactions.*/
	private var target: InputEventHandler<EditInputEventContext>? = null

	/** Gateway to the tooltip system.*/
	private val tooltipHandler: TooltipHandler = TooltipHandler(eventBus)

	init {
		eventBus.register(PreferencesChangedEvent::class) {
			System.invokeLater {
				updateStatus()
			}
		}
	}

	/** ---- [Tool] interface */

	override fun activate() {
		editor.view.setCursor(Cursor.DEFAULT)
		updateStatus()
	}

	private fun updateStatus() {
		if (editor.currentTool === this) {
			Status.set(
				StatusType.Tool,
				"${Translations.getString("edit.tool.select.zoom.text")}. ${CurrentPanMethod.panMethod.description}"
			)
		}
	}

	override fun keyPressed(e: KeyEvent) {
		if (!editor.view.editable) {
			return
		}
		LOG.trace("keyPressed")
		val context = keyEventContext(e)
		if (target != null) {
			target = target?.keyPressed(context)
			return
		}
		if (editor.dragManager.isMoveKey(e) && editor.view.selectionManager.selectionCount > 0) {
			editor.dragManager.moveByKeyEvent(e)
		} else {
			editor.dragManager.keyPressed(context)
		}
	}

	override fun keyReleased(e: KeyEvent) {
		if (!editor.view.editable) {
			return
		}
		LOG.trace("keyReleased")
		val context = keyEventContext(e)
		editor.dragManager.keyReleased(context)
		target = target?.keyReleased(context)
	}

	override fun mouseClicked(e: MouseEvent, x: Double, y: Double) {
		if (!editor.view.editable) {
			return
		}

		LOG.trace("mouseClicked at $x,$y")
		if (target != null) {
			target = target?.mouseClicked(mouseEventContext(e, x, y))
			if (target != null) {
				return
			}
		}
		target = editor.view.getInputEventHandler(e).mouseClicked(mouseEventContext(e, x, y))
	}

	override fun mouseMoved(e: MouseEvent, x: Double, y: Double) {
		super<ToolAdapter>.mouseMoved(e, x, y)
		if (!editor.view.editable) {
			return
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("mouseMoved to $x,$y")
		}

		val context = mouseEventContext(e, x, y)
		if (target != null) {
			target = target?.mouseMoved(context)
			if (target != null) {
				return
			}
		}
		target = editor.view.getInputEventHandler(e).mouseMoved(context)
		if (target == null) {
			updateCursor(editor.drawing.getDrawableAt(x, y))
		}
		tooltipHandler.handle(editor.view, editor.drawing, context)
	}

	override fun mousePressed(e: MouseEvent, x: Double, y: Double) {
		if (!editor.view.editable) {
			selectionLogic(e, x, y, allowRubberband = false)
			return
		}

		tooltipHandler.clear(editor.view)

		if (e.button != Button.BUTTON1) {
			return
		}

		LOG.trace("mousePressed at $x,$y")

		if (target != null) {
			target = target?.mousePressed(mouseEventContext(e, x, y))
			if (target != null) {
				return
			}
		}

		// Try to forward event to an interested [Drawable] in the [View]
		target = editor.view.getInputEventHandler(e).mousePressed(mouseEventContext(e, x, y))

		selectionLogic(e, x, y, allowRubberband = true)
	}

	private fun selectionLogic(e: MouseEvent, x: Double, y: Double, allowRubberband: Boolean) {
		if (CurrentPanMethod.panMethod.isActivatedByPressed(e)) {
			return
		}

		val component: Component? = editor.drawing.getDrawableAt(x, y)
		if (component != null) {
			val scope = mutableListOf(component)
			if (e.isMetaDown) {
				val buddies = mutableSetOf<Component>()
				component.collectSelectBuddies(editor.drawing, buddies)
				scope.addAll(buddies)
			}
			if (e.isShiftDown) {
				if (editor.view.selectionManager.isSelected(component)) {
					LOG.trace("Removing component from selection")
					editor.view.selectionManager.deselect(scope)
				} else {
					LOG.trace("Adding component to selection")
					editor.view.selectionManager.select(scope)
				}
			} else {
				if (!editor.view.selectionManager.isSelected(component)) {
					LOG.debug("Select single component at ${x.toInt()}/${y.toInt()}")
					editor.view.selectionManager.deselectAll()
					editor.view.selectionManager.select(scope)
				}
			}

			editor.dragManager.prepareDrag(component, x, y)

			target = editor.view.getInputEventHandler(e).mousePressed(mouseEventContext(e, x, y))
		} else {
			if (!e.isShiftDown) {
				editor.view.selectionManager.deselectAll()
			}
			if (allowRubberband) {
				LOG.trace("delegating to rubberband")
				target = rubberBandHandler
				target?.mousePressed(mouseEventContext(e, x, y))
			}
		}
	}

	override fun mouseDragged(e: MouseEvent, x: Double, y: Double) {
		if (!editor.view.editable) {
			return
		}

		if (!e.isLeftButtonDown) {
			LOG.trace("Drag with other than button 1: ${e.button.name}")
			return
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("drag to $x,$y, target is $target")
		}

		val context = mouseEventContext(e, x, y)
		if (target != null) {
			target = target?.mouseDragged(context)
			if (target != null) {
				return
			}
		}

		editor.dragManager.mouseDragged(context)
	}

	override fun mouseReleased(e: MouseEvent, x: Double, y: Double) {
		if (!editor.view.editable) {
			return
		}

		if (e.button != Button.BUTTON1) {
			return
		}

		LOG.trace("mouseReleased at $x,$y")

		val context = mouseEventContext(e, x, y)
		if (target != null) {
			target = target?.mouseReleased(context)
		}

		editor.dragManager.mouseReleased(context)

		if (target == null) {
			updateCursor(editor.drawing.getDrawableAt(x, y))
		}
	}

	/** ---- [SelectionToolImpl] */

	override fun dispose() {
		tooltipHandler.dispose()
	}
}