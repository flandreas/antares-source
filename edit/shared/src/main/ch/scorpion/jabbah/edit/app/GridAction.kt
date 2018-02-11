package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Grid

/**
 * An action for toggling the visibility of the currently active [View]'s [Grid].
 */
class GridAction(
        eventBus: EventBus = BaseModule.eventBus,
        viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractViewAction("view.action.grid", eventBus, viewManager), PropertyChangeListener<Any> {

    init {
        setView(null, viewManager.activeView)
    }

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        val view = viewManager.activeView
        if (view is DrawingView<*>) {
            view.showGrid = !view.showGrid
        }
    }

    override fun propertyChanged(e: PropertyChangeEvent<Any>) {
        if (e.name == DrawingView.PROP_SHOW_GRID) {
            updateState()
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
            selected = (viewManager.activeView as DrawingView<*>).showGrid
        }
    }
}