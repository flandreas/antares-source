package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.ViewManager
import java.awt.event.ActionEvent
import javax.swing.Action

/**
 * An action for toggling the visibility of the currently active [View]'s [Grid].
 */
class GridAction(
    viewManager: ViewManager,
    eventBus: EventBus
) : AbstractViewAction("view.action.grid", eventBus, viewManager), PropertyChangeListener<Any> {

    init {
        setView(null, viewManager.activeView)
    }

    override fun propertyChanged(e: PropertyChangeEvent<Any>) {
        updateState()
    }

    override fun actionPerformed(e: ActionEvent?) {
        val view = viewManager.activeView
        if (view is DrawingView<*>) {
            view.showGrid = !view.showGrid
        }
    }

    override fun activeViewChanged(oldView: View<out InputEventContext>?, newView: View<out InputEventContext>?) {
        setView(oldView, newView)
    }

    private fun setView(oldView: View<out InputEventContext>?, newView: View<out InputEventContext>?) {
        oldView?.removePropertyChangeListener(this)
        newView?.addPropertyChangeListener(this)
        updateState()
    }

    private fun updateState() {
        if (viewManager.activeView is DrawingView<*>) {
            putValue(Action.SELECTED_KEY, (viewManager.activeView as DrawingView<*>).showGrid)
        }
    }
}