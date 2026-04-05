package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.view.AbstractViewAction
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.app.oscilloscope.OscilloscopeDisplayEvent
import io.antarescircuit.jabbah.graph.view.app.oscilloscope.OscilloscopeViewService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

/** An [Action] for toggling the visibility of the currently active [DrawingView]'s [GraphView].*/
class OscilloscopeVisibilityAction(
	private val applicationContextHolder: GraphApplicationContextHolder,
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus,
	private val service: OscilloscopeViewService = GraphViewModule.oscilloscopeViewService
) : AbstractViewAction("graph.action.oscilloscope", eventBus, viewManager) {

	private val oscilloscopeDisplayHandler: EventHandler<OscilloscopeDisplayEvent> = { updateState() }

	private val activationStateHandler: EventHandler<SchedulerActivationStateEvent> = { updateEnabled() }

	init {
		eventBus.register(OscilloscopeDisplayEvent::class, oscilloscopeDisplayHandler)
		eventBus.register(SchedulerActivationStateEvent::class, activationStateHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(oscilloscopeDisplayHandler)
		eventBus.unregister(activationStateHandler)
	}

	override fun execute(event: io.antarescircuit.jabbah.base.event.ActionEvent) {
		val view = viewManager.activeView?.view
		if (view is DrawingView<*>) {
			if (selected) {
				service.displayOscilloscope(view as DrawingView<GraphView>)
			} else {
				service.hideOscilloscope(view as DrawingView<GraphView>)
			}
		}
	}

	override fun notifyActiveViewChanged() {
		super.notifyActiveViewChanged()
		updateState()
	}

	override fun handleViewPropertyChanged(e: PropertyChangeEvent<Any>) {
		super.handleViewPropertyChanged(e)
		if (e.name == DrawingView.PROP_EDITABLE) {
			updateEnabled()
		}
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled()
			&& viewManager.activeView?.view is DrawingView<*>
			&& (viewManager.activeView?.view as DrawingView<*>).drawing is GraphView
			&& (viewManager.activeView?.view as DrawingView<*>).editable
			&& !applicationContextHolder.scheduler.isActive

	private fun updateState() {
		selected = viewManager.activeView?.view is DrawingView<*>
			&& (viewManager.activeView!!.view as DrawingView<*>).drawing is GraphView
			&& service.isOscilloscopeDisplayed((viewManager.activeView!!.view as DrawingView<*>).drawing as GraphView)
	}
}