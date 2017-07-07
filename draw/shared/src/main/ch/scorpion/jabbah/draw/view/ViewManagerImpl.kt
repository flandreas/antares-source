package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View

/**
 * Standard implementation of the [ViewManager] interface.
 */
class ViewManagerImpl(val eventBus: EventBus) : ViewManager {

    @Suppress("unused")
    constructor(): this(BaseModule.eventBus)

    override var activeView: View<out InputEventContext>? = null
        set(value) {
            val oldView = field
            field = value
            eventBus.post(ActiveViewChangedEvent(this, oldView, field))
        }


    private val views = mutableListOf<View<out InputEventContext>>()

    /** ---- [ViewManager] interface */

    override fun registerView(view: View<out InputEventContext>) {
        if (!views.contains(view)) {
            views.add(view)
        }
    }

    override fun unregisterView(view: View<out InputEventContext>) {
        if (view == activeView) {
            activeView = null
        }
        views.remove(view)
    }
}