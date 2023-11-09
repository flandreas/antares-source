package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule.properties
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.SelectionManager

/**
 * Handles [RubberBand] interactions and selects [Component]s while rubberbanding.
 *
 * @param rubberBand the drawable [RubberBand] that is controlled by this [RubberBandHandler]
 */
class RubberBandHandler(
	private val rubberBand: RubberBand
) : InputEventHandlerAdapter<EditInputEventContext>() {

    companion object {
		private val LOG by logger(RubberBandHandler::class)
	    const val PROP_SELECT_TARGET_STRATEGY = "edit.select.rubberBandHandler.selectionTargetStrategy"
	    const val PROP_SELECT_DELAY_MS = "edit.select.rubberBandHandler.selectDelayMs"
    }

	/**
	 * Delays evaluation of the selection within the [RubberBand] by [PROP_SELECT_DELAY_MS] milliseconds
	 * to avoid costly evaluation with every MOUSE_DRAGGED event. Can be disabled by not setting the
	 * property, or by using the value 0. Editable for disabling the feature during tests.
	 */
	var delaySelectTimer: Timer? = System.createTimer()
		set(value) {
			field = initializeTimer(value)
		}

	private val currentSelection = mutableListOf<Component>()

	/** Temporarily stores the values used for asynchronously evaluating the selection within the [RubberBand].*/
	private lateinit var context: EditInputEventContext

	private val selectionTargetStrategy: SelectionTargetStrategy by lazy {
		SelectionTargetStrategy.withName(properties.getString(PROP_SELECT_TARGET_STRATEGY))
	}

	init {
		initializeTimer(delaySelectTimer)
	}

	/** ---- [InputEventHandler] */

	override fun keyPressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		if (context.keyEvent?.key == KeyEvent.VK_ALT) {
			this.context = context
			performSelection(true)
		}
		return this
	}

    override fun keyReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
	    if (context.keyEvent?.key == KeyEvent.VK_ALT) {
		    this.context = context
		    performSelection(false)
	    }
	    return this
    }

	override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mousePressed(context)
		captureCurrentSelection(context.drawingView.selectionManager)
        return rubberBand.inputEventHandler.mousePressed(context)
    }

    override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
        super.mouseDragged(context)
        rubberBand.inputEventHandler.mouseDragged(context)
	    requestExpandSelection(context)
        return this
    }

    override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        super.mouseReleased(context)
        rubberBand.inputEventHandler.mouseReleased(context)
	    if (LOG.isDebugEnabled() && rubberBand is RectangularRubberBand) {
			if (rubberBand.widthInt > 0 || rubberBand.heightInt > 0) {
				LOG.debug("Rectangular selection from ${rubberBand.xInt}/${rubberBand.yInt} to ${rubberBand.xInt + rubberBand.widthInt}/${rubberBand.yInt + rubberBand.heightInt}")
			}
	    }
        return null
    }

	/** ---- [RubberBandHandler] */

	private fun captureCurrentSelection(selectionManager: SelectionManager) {
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
			timer.initialize(delay, repeats = false) { performSelection(context.mouseEvent?.isAltDown == true) }
			timer
		} else {
			null
		}
	}

	private fun performSelection(isOther: Boolean) {
		effectiveTargetStrategy(isOther).select(
			context,
			currentSelection,
			rubberBand
		)
	}

	private fun requestExpandSelection(context: EditInputEventContext) {
		if (delaySelectTimer != null) {
			if (!delaySelectTimer!!.isRunning()) {
				this.context = context
				delaySelectTimer!!.start()
			}
		} else {
			this.context = context
			effectiveTargetStrategy(context.mouseEvent?.isAltDown == true).select(this.context, this.currentSelection, rubberBand)
		}
	}

	private fun effectiveTargetStrategy(isOther: Boolean) =
		if (isOther) {
			selectionTargetStrategy.other
		} else {
			selectionTargetStrategy
		}

	/** Determines the what [Component]s are selected in relation to the current [RubberBand] geometry.*/
	enum class SelectionTargetStrategy(
		override val customName: String,
		private val nameKey: String
	): EnumProperty<SelectionTargetStrategy> {

		/** Selects [Component]s that are completely enclosed in the [RubberBand]. */
		CONTAINS("contains", "edit.preferences.RubberBand.targetStrategy.contains") {
			override fun select(context: EditInputEventContext, currentSelection: MutableList<Component>, rubberBand: RubberBand) {
				context.drawingView.selectionManager.replace {
					currentSelection.contains(it) || (it.visible && rubberBand.contains(it.boundingBox))
				}
			}
		},

		/** Selects [Component]s that are completely enclosed or intersect the [RubberBand]. */
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

		val other: SelectionTargetStrategy get() =
			when (this) {
				CONTAINS -> INTERSECTS
				INTERSECTS -> CONTAINS
			}
	}
}