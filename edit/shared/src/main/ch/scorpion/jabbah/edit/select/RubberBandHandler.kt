package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.Timer
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
class RubberBandHandler(
	private val rubberBand: RubberBand
) : InputEventHandlerAdapter<EditInputEventContext>() {

    companion object {
        const val PROP_SELECT_STRATEGY = "edit.select.rubberBandHandler.selectionStrategy"
	    const val PROP_SELECT_DELAY_MS = "edit.select.rubberBandHandler.selectDelayMs"
    }

    enum class SelectionStrategy {

        SELECT_ON_DRAG {

	        override fun mousePressed(rubberBand: RubberBand, context: EditInputEventContext) {
				captureCurrentSelection(context.drawingView().selectionManager)
	        }

            override fun mouseDragged(rubberBand: RubberBand, context: EditInputEventContext) {
	            if (delaySelectTimer != null) {
		            if (!delaySelectTimer!!.isRunning()) {
			            this.rubberBand = rubberBand
			            this.context = context
			            delaySelectTimer!!.start()
		            }
	            } else {
		            this.rubberBand = rubberBand
		            this.context = context
		            selectWithin()
	            }
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
				selectWithin()
            }
        };

	    protected val currentSelection = mutableListOf<Component>()

	    /** Temporarily stores the values used for evaluating the selection within the rubberband.*/
	    lateinit var rubberBand: RubberBand
	    lateinit var context: EditInputEventContext

	    /**
	     * Delays evaluation of the selection within the rubberband by [PROP_SELECT_DELAY_MS] milliseconds
	     * to avoid costly evaluation with every MOUSE_DRAGGED event. Can be disabled by not setting the
	     * property, or by using the value 0.
	     *
	     * Editable for disabling the feature during tests.
	     */
	    var delaySelectTimer: Timer? = null

	    abstract fun mousePressed(rubberBand: RubberBand, context: EditInputEventContext)
        abstract fun mouseDragged(rubberBand: RubberBand, context: EditInputEventContext)
        abstract fun mouseReleased(rubberBand: RubberBand, context: EditInputEventContext)

		protected fun captureCurrentSelection(selectionManager: SelectionManager) {
			// The SelectionTool makes sure that the currentSelection is empty if SHIFT is pressed
			currentSelection.clear()
			currentSelection.addAll(selectionManager.selection)
		}

	    fun selectWithin() {
		    context.drawingView().selectionManager.replace {
			    currentSelection.contains(it) || (it.visible && rubberBand.contains(it.boundingBox))
		    }
	    }

    }

    val selectionStrategy: SelectionStrategy = BaseModule.properties.get(PROP_SELECT_STRATEGY)

	init {
		selectionStrategy.delaySelectTimer = BaseModule.properties.getOptional<Int>(PROP_SELECT_DELAY_MS)?.let { delay ->
			if (delay > 0) {
				System.createTimer().also {
					it.initialize(delay, repeats = false) {
						selectionStrategy.selectWithin()
					}
				}
			} else {
				null
			}
		}
	}

	/** ---- [InputEventHandler] */

    override fun keyReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
	    // Avoid stop dragging Rubberband when SHIFT is released
	    return this
    }

	override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mousePressed(context)
	    selectionStrategy.mousePressed(rubberBand, context)
        return rubberBand.inputEventHandler.mousePressed(context)
    }

    override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
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