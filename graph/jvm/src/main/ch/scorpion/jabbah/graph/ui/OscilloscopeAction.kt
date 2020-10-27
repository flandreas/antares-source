package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.app.OscilloscopeDisplayEvent
import ch.scorpion.jabbah.graph.view.app.OscilloscopeViewService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/** An [Action] for toggling the visibility of the currently active [DrawingView]'s [GraphView].*/
class OscilloscopeAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus,
	private val service: OscilloscopeViewService = GraphViewModule.oscilloscopeViewService
) : AbstractViewAction("graph.action.oscilloscope", eventBus, viewManager) {

	private val oscilloscopeDisplayHandler: EventHandler<OscilloscopeDisplayEvent> = { updateState() }

	init {
		eventBus.register(OscilloscopeDisplayEvent::class, oscilloscopeDisplayHandler)
	}

	override fun calculateEnabled(): Boolean {
		// Disable until entire Oscilloscope has been improved
		//return false
		return AppModule.userHolder.user.isDeveloper
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(oscilloscopeDisplayHandler)
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val view = viewManager.activeView
		if (view is DrawingView<*>) {
			val graphView = ((viewManager.activeView as DrawingView<*>).drawing) as GraphView
			if (selected) {
				service.displayOscilloscope(graphView)
			} else {
				service.hideOscilloscope(graphView)
			}
		}
	}

	override fun notifyActiveViewChanged() {
		super.notifyActiveViewChanged()
		updateState()
	}

	private fun updateState() {
		selected = viewManager.activeView is DrawingView<*>
			&& (viewManager.activeView as DrawingView<*>).drawing is GraphView
			&& service.isOscilloscopeDisplayed((viewManager.activeView as DrawingView<*>).drawing as GraphView)
	}
}