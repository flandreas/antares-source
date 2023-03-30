package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule.properties
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
	    const val PROP_SELECT_TARGET_STRATEGY = "edit.select.rubberBandHandler.selectionTargetStrategy"
	    const val PROP_SELECT_DELAY_MS = "edit.select.rubberBandHandler.selectDelayMs"
    }

    enum class SelectionTimeStrategy {

        SELECT_ON_DRAG {

	        override fun mousePressed(rubberBand: RubberBand, context: EditInputEventContext, selectionTargetStrategy: SelectionTargetStrategy) {
				captureCurrentSelection(context.drawingView.selectionManager)
	        }

            override fun mouseDragged(rubberBand: RubberBand, context: EditInputEventContext, selectionTargetStrategy: SelectionTargetStrategy) {
	            if (delaySelectTimer != null) {
		            if (!delaySelectTimer!!.isRunning()) {
			            this.rubberBandRef = rubberBand
			            this.context = context
			            this.selectionTargetStrategy = selectionTargetStrategy
			            delaySelectTimer!!.start()
		            }
	            } else {
		            this.rubberBandRef = rubberBand
		            this.context = context
		            this.selectionTargetStrategy = selectionTargetStrategy
		            selectionTargetStrategy.select(this.context, this.currentSelection, this.rubberBandRef)
	            }
            }

            override fun mouseReleased(rubberBand: RubberBand, context: EditInputEventContext, selectionTargetStrategy: SelectionTargetStrategy) {
	            // empty
            }
        },

        /** Updates the selection not before the mouse has been released.*/
        SELECT_ON_RELEASE {

	        override fun mousePressed(rubberBand: RubberBand, context: EditInputEventContext, selectionTargetStrategy: SelectionTargetStrategy) {
		        captureCurrentSelection(context.drawingView.selectionManager)
	        }
            override fun mouseDragged(rubberBand: RubberBand, context: EditInputEventContext, selectionTargetStrategy: SelectionTargetStrategy) {
                // empty
            }

            override fun mouseReleased(rubberBand: RubberBand, context: EditInputEventContext, selectionTargetStrategy: SelectionTargetStrategy) {
	            selectionTargetStrategy.select(this.context, currentSelection, rubberBand)
            }
        };

	    val currentSelection = mutableListOf<Component>()

	    /** Temporarily stores the values used for evaluating the selection within the rubberband.*/
	    lateinit var rubberBandRef: RubberBand
	    lateinit var context: EditInputEventContext
		lateinit var selectionTargetStrategy: SelectionTargetStrategy

	    /**
	     * Delays evaluation of the selection within the rubberband by [PROP_SELECT_DELAY_MS] milliseconds
	     * to avoid costly evaluation with every MOUSE_DRAGGED event. Can be disabled by not setting the
	     * property, or by using the value 0.
	     *
	     * Editable for disabling the feature during tests.
	     */
	    var delaySelectTimer: Timer? = null
		    set(value) {
				field = initializeTimer(value)
			}

	    abstract fun mousePressed(rubberBand: RubberBand, context: EditInputEventContext, selectionTargetStrategy: SelectionTargetStrategy)
        abstract fun mouseDragged(rubberBand: RubberBand, context: EditInputEventContext, selectionTargetStrategy: SelectionTargetStrategy)
        abstract fun mouseReleased(rubberBand: RubberBand, context: EditInputEventContext, selectionTargetStrategy: SelectionTargetStrategy)

		protected fun captureCurrentSelection(selectionManager: SelectionManager) {
			// The SelectionTool makes sure that the currentSelection is empty if SHIFT is pressed
			currentSelection.clear()
			currentSelection.addAll(selectionManager.selection)
		}

	    private fun initializeTimer(timer: Timer?): Timer? {
		    if (timer == null) {
				return null
		    }
		    val delay = properties.getOptional<Int>(PROP_SELECT_DELAY_MS)
		    return if (delay != null) {
			    timer.initialize(delay, repeats = false) {
				    selectionTargetStrategy.select(
					    context,
					    currentSelection,
					    rubberBandRef
				    )
			    }
			    timer
		    } else {
			    null
		    }
	    }
    }

	enum class SelectionTargetStrategy(
		override val customName: String,
		private val nameKey: String
	): EnumProperty<SelectionTargetStrategy> {

		CONTAINS("contains", "edit.preferences.RubberBand.targetStrategy.contains") {
			override fun select(context: EditInputEventContext, currentSelection: MutableList<Component>, rubberBand: RubberBand) {
				context.drawingView.selectionManager.replace {
					currentSelection.contains(it) || (it.visible && rubberBand.contains(it.boundingBox))
				}
			}
		},
		INTERSECTS("intersects", "edit.preferences.RubberBand.targetStrategy.intersects") {
			override fun select(context: EditInputEventContext, currentSelection: MutableList<Component>, rubberBand: RubberBand) {
				context.drawingView.selectionManager.replace {
					currentSelection.contains(it) || (it.visible && rubberBand.intersects(it.boundingBox))
				}
			}
		};

		companion object {
			fun withName(name: String): SelectionTargetStrategy =
				values().firstOrNull { it.customName == name } ?: throw IllegalArgumentException("unknown SelectionTargetStrategy '$name'")
		}

		abstract fun select(context: EditInputEventContext, currentSelection: MutableList<Component>, rubberBand: RubberBand)

		override fun toString(): String = Translations.getString(nameKey)
	}

	val selectionTimeStrategy: SelectionTimeStrategy by lazy { properties.get(PROP_SELECT_STRATEGY) }

	private val selectionTargetStrategy: SelectionTargetStrategy by lazy {
		SelectionTargetStrategy.withName(properties.getString(PROP_SELECT_TARGET_STRATEGY))
	}

	init {
		selectionTimeStrategy.delaySelectTimer = System.createTimer()
	}

	/** ---- [InputEventHandler] */

    override fun keyReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
	    // Avoid stop dragging Rubberband when SHIFT is released
	    return this
    }

	override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mousePressed(context)
	    selectionTimeStrategy.mousePressed(rubberBand, context, selectionTargetStrategy)
        return rubberBand.inputEventHandler.mousePressed(context)
    }

    override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
        super.mouseDragged(context)
        rubberBand.inputEventHandler.mouseDragged(context)
        selectionTimeStrategy.mouseDragged(rubberBand, context, selectionTargetStrategy)
        return this
    }

    override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mouseReleased(context)
        rubberBand.inputEventHandler.mouseReleased(context)
        selectionTimeStrategy.mouseReleased(rubberBand, context, selectionTargetStrategy)
        return null
    }
}