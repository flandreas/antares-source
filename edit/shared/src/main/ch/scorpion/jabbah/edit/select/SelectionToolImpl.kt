package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.snap.MultiComponentSnappable
import ch.scorpion.jabbah.edit.tool.ToolAdapter
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.view.TooltipHandler
import ch.scorpion.jabbah.edit.*

/**
 * Standard implementation of a [SelectionTool].
 * Uses a [RubberBandHandler] for selecting multiple [Component]s at a time.
 */
class SelectionToolImpl(
        editor: Editor,
        private val rubberBandHandler: RubberBandHandler,
        val eventBus: EventBus
) : ToolAdapter(editor), SelectionTool {

    companion object {
        private val LOG by logger(SelectionToolImpl::class)
    }

    /** The target [InputEventHandler] to which events are forwarded during complex interactions.*/
    private var target: InputEventHandler<EditInputEventContext>? = null

    /** The [Component] acting as reference for moving potentially many [Component]s. */
    private var movedReferenceComponent: Component? = null

    /** The location of [movedReferenceComponent] before moving it. */
    private var moveStartLocation = Point2D()

    /** Stores the location of [movedReferenceComponent] before the last drag operation.*/
    private var moveLastLocation = Point2D()

    /** Support for snapping multiple [Component]s while being moved. Initialized when starting to drag.*/
    private var multiComponentSnappable: MultiComponentSnappable? = null

    /** Gateway to the tooltip system.*/
    private val tooltipHandler: TooltipHandler = TooltipHandler(eventBus)

    /** ---- [Tool] interface */

    override fun activate() {
        editor.view.setCursor(Cursor.DEFAULT)
    }

    override fun keyPressed(e: KeyEvent) {
        target = target?.keyPressed(keyEventContext(e))
    }

    override fun keyReleased(e: KeyEvent) {
        target = target?.keyReleased(keyEventContext(e))
    }

    override fun mouseClicked(e: MouseEvent, x: Double, y: Double) {
        LOG.debug("SelectionToolImpl: mouseClicked at $x,$y")
        if (target != null) {
            target = target?.mouseClicked(mouseEventContext(e, x, y))
            if (target != null) {
                return
            }
        }
        target = editor.view.getInputEventHandler(e).mouseClicked(mouseEventContext(e, x, y))
    }

    override fun mouseMoved(e: MouseEvent, x: Double, y: Double) {
        LOG.trace("SelectionToolImpl: mouseMoved to $x,$y")

        if (target != null) {
            target = target?.mouseMoved(mouseEventContext(e, x, y))
            if (target != null) {
                return
            }
        }
        target = editor.view.getInputEventHandler(e).mouseMoved(mouseEventContext(e, x, y))
        if (target == null) {
            updateCursor(editor.drawing.getDrawableAt(x, y))
        }
        tooltipHandler.handle(editor.view, editor.drawing, x, y)
    }

    override fun mousePressed(e: MouseEvent, x: Double, y: Double) {
        tooltipHandler.clear(editor.view)

	    if (e.button == Button.BUTTON3) {
		    eventBus.post(ContextActionRequest(editor))
		    e.consume()
		    return
	    }

        if (e.button != Button.BUTTON1) {
            return
        }

        LOG.debug("SelectionToolImpl: mousePressed at $x,$y")

        if (target != null) {
            target = target?.mousePressed(mouseEventContext(e, x, y))
            if (target != null) {
                return
            }
        }

        // Try to forward event to an interested [Drawable] in the [View]
        target = editor.view.getInputEventHandler(e).mousePressed(mouseEventContext(e, x, y))

        // Selection logic
        val component: Component? = editor.drawing.getDrawableAt(x, y)
        if (component != null) {
            if (e.isShiftDown) {
                if (editor.view.selectionManager.isSelected(component)) {
                    LOG.debug("SelectionToolImpl: Removing component from selection")
                    editor.view.selectionManager.deselect(component)
                } else {
                    LOG.debug("SelectionToolImpl: Adding component to selection")
                    editor.view.selectionManager.select(component)
                }
            } else {
                if (!editor.view.selectionManager.isSelected(component)) {
                    LOG.debug("Selecting only single component")
                    editor.view.selectionManager.deselectAll()
                    editor.view.selectionManager.select(component)
                }
            }

            movedReferenceComponent = component
            moveStartLocation = Point2D(movedReferenceComponent!!.location)
            moveLastLocation = Point2D(x, y)

	        target = editor.view.getInputEventHandler(e).mousePressed(mouseEventContext(e, x, y))
        } else {
            if (!e.isShiftDown) {
                editor.view.selectionManager.deselectAll()
            }
            LOG.debug("SelectionToolImpl: delegating to rubberband")
            target = rubberBandHandler
            target?.mousePressed(mouseEventContext(e, x, y))
        }
    }

    override fun mouseDragged(e: MouseEvent, x: Double, y: Double) {
        if (e.button != Button.BUTTON1) {
            return
        }

        LOG.trace("SelectionTool: drag to $x,$y, target is $target")

        if (target != null) {
            target = target?.mouseDragged(mouseEventContext(e, x, y))
            if (target != null) {
                return
            }
        }

        val dx = x - moveLastLocation.x
        val dy = y - moveLastLocation.y
        val selection = editor.view.selectionManager.selection
        var offset = Point2D()

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
        selection.forEach { it.prepareMoveBy(selection) }
        selection.forEach { it.moveBy(dx + offset.x, dy + offset.y) }
        selection.forEach { it.completeMoveBy() }

        eventBus.post(DragEvent(editor, selection))
        moveLastLocation = Point2D(x + offset.x, y + offset.y)
        editor.drawing.validate()
    }

    override fun mouseReleased(e: MouseEvent, x: Double, y: Double) {
        if (e.button != Button.BUTTON1) {
            return
        }

        LOG.debug("SelectionTool: mouseReleased at $x,$y")

        if (target != null) {
            target = target?.mouseReleased(mouseEventContext(e, x, y))
        }

        if (movedReferenceComponent != null) {
            if (moveStartLocation != movedReferenceComponent?.location) {
                try {
                    editor.commandManager.register(MoveCommand(
                        editor,
                        editor.view.selectionManager.selection,
                        movedReferenceComponent!!.location.subtract(moveStartLocation)))
                } catch(e: Throwable) {
                    LOG.error("SelectionToolImpl.mouseReleased(): error '${e.message}'")
                    editor.commandManager.rollbackTransaction()
                }
            }
        }

        // Cleanup
        target = null
        multiComponentSnappable = null
        movedReferenceComponent = null
        updateCursor(null)
    }

    /** ---- [SelectionToolImpl] */

    private fun keyEventContext(e: KeyEvent): EditInputEventContext {
        return EditInputEventContext(editor = editor, keyEvent = e)
    }

    private fun mouseEventContext(e: MouseEvent, x: Double, y: Double): EditInputEventContext {
        return EditInputEventContext(
            editor = editor,
            mouseEvent = e,
            x = x,
            y = y
        )
    }

    private fun updateCursor(component: Drawable?) {
        if (component == null) {
            editor.view.setCursor(Cursor.DEFAULT)
        } else {
            editor.view.setCursor(Cursor.HAND)
        }
    }
}