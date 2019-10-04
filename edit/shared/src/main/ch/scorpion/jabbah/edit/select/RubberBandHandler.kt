package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.EditInputEventContext

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
		        selection.clear()
	        }

            override fun mouseDragged(rubberBand: RubberBand, context: EditInputEventContext) {
                // TODO This is a pretty inefficient solution that turns the CPU crazy due to heavy
                // repainting and calculation load. Check if there is a more efficient solution.
	            context.drawingView().selectionManager.deselect(selection)
	            selection.clear()
                context.drawingView().drawing.getDrawables()
                    .filter { it.visible && rubberBand.contains(it.boundingBox) }
                    .forEach { selection.add(it) }
                context.drawingView().selectionManager.select(selection)
            }

            override fun mouseReleased(rubberBand: RubberBand, context: EditInputEventContext) {
	            // empty
            }
        },

        /** Updates the selection not before the mouse has been released.*/
        SELECT_ON_RELEASE {

	        override fun mousePressed(rubberBand: RubberBand, context: EditInputEventContext) {
		        // empty
	        }
            override fun mouseDragged(rubberBand: RubberBand, context: EditInputEventContext) {
                // empty
            }

            override fun mouseReleased(rubberBand: RubberBand, context: EditInputEventContext) {
                context.drawingView().drawing.getDrawables()
                    .filter { it.visible && rubberBand.contains(it.boundingBox) }
                    .forEach { context.drawingView().selectionManager.select(it) }
            }
        };

        /** Holds the current selection that is updated in the [SELECT_ON_DRAG] strategy.*/
        val selection = mutableListOf<Component>()

	    abstract fun mousePressed(rubberBand: RubberBand, context: EditInputEventContext)
        abstract fun mouseDragged(rubberBand: RubberBand, context: EditInputEventContext)
        abstract fun mouseReleased(rubberBand: RubberBand, context: EditInputEventContext)

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