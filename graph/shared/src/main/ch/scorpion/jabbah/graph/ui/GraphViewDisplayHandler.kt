package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.view.TooltipHandler


/**
 * Handles events on a [GraphView] when NOT being executed or NOT being editable.
 *
 * Child [GraphView]s are generally NOT editable, and therefore the following responsibilities must be covered by
 * this [GraphViewDisplayHandler], which are covered by the [Editor] in case of a root [GraphView]:
 *
 * - Display the tooltip of the [GraphElementView] at the current mouse location
 * - Dive into a [SubGraphVerticeView] when the user double clicks on it
 */
class GraphViewDisplayHandler(
	private val view: DrawingView<GraphView>,
	private val scheduler: Scheduler,
	eventBus: EventBus
) {

	companion object {
		private val LOG by logger(GraphViewDisplayHandler::class)
	}

	private val mouseHandler = MouseHandler()

	/** Gateway to the tooltip system.*/
	private val tooltipHandler: TooltipHandler = TooltipHandler(eventBus)

	init {
		eventBus.register(SchedulerActivationStateEvent::class) { updateActivationState() }
		view.addPropertyChangeListener(object : PropertyChangeListener<Any> {
			override fun propertyChanged(e: PropertyChangeEvent<Any>) {
				if (e.name == DrawingView.PROP_DRAWING || e.name == DrawingView.PROP_EDITABLE || e.name == View.PROP_CANVAS) {
					updateActivationState()
				}
			}
		})
	}

	fun dispose() {
		passivate()
	}

	private fun updateActivationState() {
		if (!scheduler.isActive && !view.editable) {
			activate()
		} else {
			passivate()
		}
	}

	private fun activate() {
		view.addMouseListener(mouseHandler)
		view.addMouseMotionListener(mouseHandler)
	}

	private fun passivate() {
		view.removeMouseListener(mouseHandler)
		view.removeMouseMotionListener(mouseHandler)
	}

	private inner class MouseHandler : MouseAdapter() {

		/** Displays the hand cursor if the mouse is over a [SubGraphVerticeView] */
		override fun mouseMoved(e: MouseEvent) {
			LOG.trace("GraphViewDisplayHandler.mouseMoved")
			val x = view.viewToModelX(e.x.toDouble())
			val y = view.viewToModelY(e.y.toDouble())

			val drawable = view.drawing.getDrawableAt(x, y)
			tooltipHandler.handle(view, view.drawing, x, y)

			if (drawable != null /*&& drawable is SubGraphVerticeView<*>*/) {
				view.setCursor(Cursor.CLICK)
			} else {
				view.setCursor(Cursor.DEFAULT)
			}
		}

		/** Forwards a click with button 1 to a [SubGraphVerticeView] at the mouse location, if any. */
		override fun mouseClicked(e: MouseEvent) {
			tooltipHandler.clear(view)

			if (e.button != Button.BUTTON1) {
				return
			}

			val x = view.viewToModelX(e.x.toDouble())
			val y = view.viewToModelY(e.y.toDouble())

			val drawable = view.drawing.getDrawableAt(x, y)
			if (drawable != null /*&& drawable is SubGraphVerticeView*/) {
				val context = InputEventContext(
					view = view,
					mouseEvent = e,
					x = x,
					y = y,
					readonly = true)
				drawable.getInputEventHandler(context).mouseClicked(context)
				view.drawing.validate()
			}
		}
	}
}