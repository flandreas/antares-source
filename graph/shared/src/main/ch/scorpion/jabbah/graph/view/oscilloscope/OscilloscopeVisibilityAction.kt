package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.app.oscilloscope.OscilloscopeDisplayEvent
import ch.scorpion.jabbah.graph.view.app.oscilloscope.OscilloscopeViewService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/** An [Action] for toggling the visibility of the currently active [DrawingView]'s [GraphView].*/
class OscilloscopeVisibilityAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus,
	private val service: OscilloscopeViewService = GraphViewModule.oscilloscopeViewService
) : AbstractViewAction("graph.action.oscilloscope", eventBus, viewManager) {

	private val oscilloscopeDisplayHandler: EventHandler<OscilloscopeDisplayEvent> = { updateState() }

	init {
		eventBus.register(OscilloscopeDisplayEvent::class, oscilloscopeDisplayHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(oscilloscopeDisplayHandler)
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val view = viewManager.activeView
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

	private fun updateState() {
		selected = viewManager.activeView is DrawingView<*>
			&& (viewManager.activeView as DrawingView<*>).drawing is GraphView
			&& service.isOscilloscopeDisplayed((viewManager.activeView as DrawingView<*>).drawing as GraphView)
	}
}