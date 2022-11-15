package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.SelectionManager

/**
 * Handles [RubberBand] interactions and selects [Component]s while rubberbanding, or after rubberbanding is done,
 * depending on the configured strategy.
 *
 * @param rubberBand the drawable [RubberBand] that is controlled by this [RubberBandHandler]
 */
class RubberBandHandler(private val rubberBand: RubberBand) : InputEventHandlerAdapter<EditInputEventContext>() {

    companion object {
        const val PROP_SELECT_STRATEGY = "edit.select.rubberBandHandler.selectionStrategy"
    }

    enum class SelectionStrategy {

        /** Updates the selection on every mouse drag. */
        SELECT_ON_DRAG {

	        override fun mousePressed(rubberBand: RubberBand, context: EditInputEventContext) {
				captureCurrentSelection(context.drawingView().selectionManager)
	        }

            override fun mouseDragged(rubberBand: RubberBand, context: EditInputEventContext) {
				selectWithin(rubberBand, context.drawingView().selectionManager)
            }

            override fun mouseReleased(rubberBand: RubberBand, context: EditInputEventContext) {
	            // empty
            }
        },

        /** Updates the selection not before the mouse has been released.*/
        SELECT_ON_RELEASE {

	        override fun mousePressed(rubberBand: RubberBand, context: EditInputEventContext) {
		        captureCurrentSelection(context.drawingView().selectionManager)
	        }
            override fun mouseDragged(rubberBand: RubberBand, context: EditInputEventContext) {
                // empty
            }

            override fun mouseReleased(rubberBand: RubberBand, context: EditInputEventContext) {
				selectWithin(rubberBand, context.drawingView().selectionManager)
            }
        };

	    protected val currentSelection = mutableListOf<Component>()

	    abstract fun mousePressed(rubberBand: RubberBand, context: EditInputEventContext)
        abstract fun mouseDragged(rubberBand: RubberBand, context: EditInputEventContext)
        abstract fun mouseReleased(rubberBand: RubberBand, context: EditInputEventContext)

		protected fun captureCurrentSelection(selectionManager: SelectionManager) {
			// The SelectionTool makes sure that the currentSelection is empty if SHIFT is pressed
			currentSelection.clear()
			currentSelection.addAll(selectionManager.selection)
		}

	    protected fun selectWithin(rubberBand: RubberBand, selectionManager: SelectionManager) {
		    selectionManager.replace {
			    currentSelection.contains(it) || (it.visible && rubberBand.contains(it.boundingBox))
		    }
		}

    }

    private val selectionStrategy: SelectionStrategy = BaseModule.properties.get(PROP_SELECT_STRATEGY)

    /** ---- [InputEventHandler] */

    override fun keyReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
	    // Avoid stop dragging Rubberband when SHIFT is released
	    return this
    }

	override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mousePressed(context)
	    selectionStrategy.mousePressed(rubberBand, context)
        return rubberBand.inputEventHandler.mousePressed(context)
    }

    override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mouseDragged(context)
        rubberBand.inputEventHandler.mouseDragged(context)
        selectionStrategy.mouseDragged(rubberBand, context)
        return this
    }

    override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mouseReleased(context)
        rubberBand.inputEventHandler.mouseReleased(context)
        selectionStrategy.mouseReleased(rubberBand, context)
        return null
    }
}