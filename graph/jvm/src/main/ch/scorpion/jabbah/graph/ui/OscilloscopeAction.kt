package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.OscilloscopeDisplayedEvent
import ch.scorpion.jabbah.graph.view.app.OscilloscopeDisplayEvent
import ch.scorpion.jabbah.graph.view.app.OscilloscopeViewService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import java.awt.event.ActionEvent
import javax.swing.Action

/** An [Action] for toggeling the visibility of the currently active [DrawingView]'s [GraphView].*/
class OscilloscopeAction(
        viewManager: ViewManager,
        eventBus: EventBus,
        private val service: OscilloscopeViewService = GraphViewModule.oscilloscopeViewService
) : AbstractViewAction("graph.action.oscilloscope", eventBus, viewManager) {

    init {
        eventBus.register(OscilloscopeDisplayEvent::class, { updateState() })
    }

    override fun actionPerformed(e: ActionEvent?) {
        val view = viewManager.activeView
        if (view is DrawingView<*>) {
            val graphView = ((viewManager.activeView as DrawingView<*>).drawing) as GraphView<GraphElementView<*>>
            service.displayOscilloscope(getValue(Action.SELECTED_KEY) as Boolean, graphView)
        }
    }

    override fun activeViewChanged(oldView: View<out InputEventContext>?, newView: View<out InputEventContext>?) {
        super.activeViewChanged(oldView, newView)
        updateState()
    }

    private fun updateState() {
            putValue(Action.SELECTED_KEY, viewManager.activeView is DrawingView<*>
                    && (viewManager.activeView as DrawingView<*>).drawing is GraphView<*>
                    && service.isOscilloscopeDisplayed((viewManager.activeView as DrawingView<*>).drawing as GraphView<GraphElementView<*>>)
            )
    }
}