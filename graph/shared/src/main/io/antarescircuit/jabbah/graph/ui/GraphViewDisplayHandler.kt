package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.draw.view.TooltipHandler
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView


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
	private val view: DrawingView<GraphElementView<*>, GraphView>,
	private val applicationContextHolder: GraphApplicationContextHolder,
	private val eventBus: EventBus
) {

	companion object {
		private val LOG by logger(GraphViewDisplayHandler::class)
	}

	private val mouseHandler = MouseHandler()

	/** Gateway to the tooltip system.*/
	private val tooltipHandler: TooltipHandler = TooltipHandler(eventBus)

	private val schedulerActivationHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (it.scheduler === applicationContextHolder.scheduler) {
			updateActivationState()
		}
	}

	init {
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationHandler)

		view.addPropertyChangeListener(object : PropertyChangeListener<Any> {
			override fun propertyChanged(e: PropertyChangeEvent<Any>) {
				if (e.name == DrawingView.PROP_DRAWING || e.name == DrawingView.PROP_EDITABLE || e.name == View.PROP_CANVAS) {
					updateActivationState()
				}
			}
		})
	}

	fun dispose() {
		eventBus.unregister(SchedulerActivationStateEvent::class, schedulerActivationHandler)
		tooltipHandler.dispose()
		passivate()
	}

	private fun updateActivationState() {
		if (!applicationContextHolder.scheduler.isActive && !view.editable) {
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
		tooltipHandler.clear(view)
		view.removeMouseListener(mouseHandler)
		mouseHandler.resetMouseGesture()
		view.removeMouseMotionListener(mouseHandler)
	}

	private inner class MouseHandler : ReleaseClickMouseAdapter(Editor.DRAG_THRESHOLD.toDouble()) {

		private var pressedDrawable: Any? = null

		override fun onMousePressed(e: MouseEvent) {
			pressedDrawable = view.drawing.getDrawableAt(view.viewToModel(e.location))
		}

		/** Displays the hand cursor if the mouse is over a [SubGraphVerticeView] */
		override fun mouseMoved(e: MouseEvent) {
			LOG.trace("GraphViewDisplayHandler.mouseMoved")
			val p = view.viewToModel(e.location)

			val drawable = view.drawing.getDrawableAt(p)
			tooltipHandler.handle(view, view.drawing, mouseEventContext(e, p.x, p.y, !view.editable))

			if (drawable != null /*&& drawable is SubGraphVerticeView<*>*/) {
				view.setCursor(Cursor.CLICK)
			} else {
				view.setCursor(Cursor.DEFAULT)
			}
		}

		/** Forwards a click with button 1 to a [SubGraphVerticeView] at the mouse location, if any. */
		override fun onMouseClicked(e: MouseEvent) {
			tooltipHandler.clear(view)

			if (e.button != Button.BUTTON1) {
				return
			}

			val p = view.viewToModel(e.location)

			val drawable = view.drawing.getDrawableAt(p)
			if (drawable != null && drawable === pressedDrawable) {
				val context = mouseEventContext(e, p.x, p.y, true)
				drawable.getInputEventHandler(context).mouseClicked(context)
				view.drawing.validate()
			}
		}

		private fun mouseEventContext(e: MouseEvent, x: Double, y: Double, readonly: Boolean): InputEventContext {
			return InputEventContext(
				view = view,
				mouseEvent = e,
				x = x,
				y = y,
				readonly = readonly)
		}
	}
}
