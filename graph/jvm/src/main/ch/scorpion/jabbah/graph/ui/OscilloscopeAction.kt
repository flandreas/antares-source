package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.OscilloscopeDisplayedEvent
import java.awt.event.ActionEvent
import javax.swing.Action

/** An [Action] for toggeling the visibility of the currently active [DrawingView]'s [GraphView].*/
class OscilloscopeAction(
        viewManager: ViewManager,
        eventBus: EventBus
) : AbstractViewAction("graph.action.oscilloscope", eventBus, viewManager) {

    init {
        eventBus.register(OscilloscopeDisplayedEvent::class, { updateState() })
    }

    override fun actionPerformed(e: ActionEvent?) {
        val view = viewManager.activeView
        if (view is DrawingView<*>) {
            val graphView = ((viewManager.activeView as DrawingView<*>).drawing) as GraphView<*>
            graphView.isOscilloscopeDisplayed = !graphView.isOscilloscopeDisplayed
        }
    }

    private fun updateState() {
        if (viewManager.activeView is DrawingView<*> && (viewManager.activeView as DrawingView<*>).drawing is GraphView<*>)
            putValue(Action.SELECTED_KEY, (((viewManager.activeView as DrawingView<*>).drawing) as GraphView<*>).isOscilloscopeDisplayed)
    }
}